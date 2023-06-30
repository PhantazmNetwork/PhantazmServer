package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.item.ItemEntityMeta;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.hologram.Hologram;
import org.phantazm.core.hologram.InstanceHologram;
import org.phantazm.core.time.TickFormatter;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Model("zombies.map.shop.interactor.slot_machine")
@Cache(false)
public class SlotMachineInteractor implements ShopInteractor {
    private final Data data;
    private final TickFormatter tickFormatter;
    private final DelayFormula delayFormula;
    private final List<SlotMachineFrame> frames;
    private final List<ShopInteractor> rollStartInteractors;
    private final List<ShopInteractor> mismatchedPlayerInteractors;
    private final List<ShopInteractor> whileRollingInteractors;
    private final List<ShopInteractor> timeoutExpiredInteractors;
    private final Random random;

    //non-null in tick and interaction methods - set in initialize(Shop)
    private Shop shop;
    private Hologram hologram;

    private Entity item;

    private PlayerInteraction rollInteraction;
    private boolean rolling;
    private boolean doneRolling;

    private long rollFinishTime;

    private long lastFrameTime;
    private int currentFrameIndex;
    private int ticksUntilNextFrame;

    @FactoryMethod
    public SlotMachineInteractor(Data data, @NotNull @Child("tick_formatter") TickFormatter tickFormatter,
            @NotNull @Child("delay_formula") DelayFormula delayFormula,
            @NotNull @Child("frames") List<SlotMachineFrame> frames,
            @NotNull @Child("roll_start_interactors") List<ShopInteractor> rollStartInteractors,
            @NotNull @Child("mismatched_player_interactors") List<ShopInteractor> mismatchedPlayerInteractors,
            @NotNull @Child("while_rolling_interactors") List<ShopInteractor> whileRollingInteractors,
            @NotNull @Child("timeout_expired_interactors") List<ShopInteractor> timeoutExpiredInteractors,
            @NotNull Random random) {
        this.data = data;
        this.tickFormatter = tickFormatter;
        this.delayFormula = delayFormula;
        this.frames = new ArrayList<>(frames);
        this.rollStartInteractors = rollStartInteractors;
        this.mismatchedPlayerInteractors = mismatchedPlayerInteractors;
        this.whileRollingInteractors = whileRollingInteractors;
        this.timeoutExpiredInteractors = timeoutExpiredInteractors;
        this.random = random;
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        this.shop = shop;
        this.hologram = new InstanceHologram(shop.center().add(0, data.hologramOffset, 0), 0);
        this.hologram.setInstance(shop.instance());
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        if (rolling) {
            if (interaction.player() != rollInteraction.player()) {
                for (ShopInteractor interactor : mismatchedPlayerInteractors) {
                    interactor.handleInteraction(interaction);
                }

                return false;
            }

            if (!doneRolling) {
                for (ShopInteractor interactor : whileRollingInteractors) {
                    interactor.handleInteraction(interaction);
                }

                return false;
            }

            SlotMachineFrame currentFrame = frames.get((currentFrameIndex - 1) % frames.size());
            for (ShopInteractor interactor : currentFrame.interactors()) {
                interactor.handleInteraction(rollInteraction);
            }

            reset();
            return true;
        }

        rolling = true;

        Collections.shuffle(frames, random);

        rollInteraction = interaction;
        lastFrameTime = System.currentTimeMillis();
        currentFrameIndex = 0;
        ticksUntilNextFrame = delayFormula.delay(data.frameCount, currentFrameIndex);

        for (ShopInteractor interactor : rollStartInteractors) {
            interactor.handleInteraction(interaction);
        }

        return false;
    }

    @Override
    public void tick(long time) {
        if (!rolling) {
            return;
        }

        if (!doneRolling) {
            long ticksSinceLastFrame = (time - lastFrameTime) / MinecraftServer.TICK_MS;
            if (ticksSinceLastFrame >= ticksUntilNextFrame) {
                displayRollFrame(currentFrameIndex);

                lastFrameTime = time;
                currentFrameIndex++;
                ticksUntilNextFrame = delayFormula.delay(data.frameCount, currentFrameIndex);
            }
        }

        if (currentFrameIndex == data.frameCount) {
            if (!doneRolling) {
                rollFinishTime = time;
            }

            doneRolling = true;
            long ticksSinceDoneRolling = (time - rollFinishTime) / MinecraftServer.TICK_MS;

            if (ticksSinceDoneRolling < data.gracePeriodTicks) {
                String timeString = tickFormatter.format(data.gracePeriodTicks - ticksSinceDoneRolling);
                TagResolver[] tags = getTagsForFrame(frames.get((currentFrameIndex - 1) % frames.size()),
                        Placeholder.unparsed("time_left", timeString));

                List<Component> newComponents = new ArrayList<>(data.gracePeriodFormats.size());
                for (String formatString : data.gracePeriodFormats) {
                    newComponents.add(MiniMessage.miniMessage().deserialize(formatString, tags));
                }

                updateHologram(newComponents);
                return;
            }

            for (ShopInteractor interactor : timeoutExpiredInteractors) {
                interactor.handleInteraction(rollInteraction);
            }

            reset();
        }
    }

    private void reset() {
        if (item != null) {
            item.remove();
            item = null;
        }

        hologram.clear();
        rollInteraction = null;
        rolling = false;
        doneRolling = false;
        rollFinishTime = 0L;
        lastFrameTime = 0L;
        currentFrameIndex = 0;
        ticksUntilNextFrame = 0;
    }

    private void displayRollFrame(int index) {
        SlotMachineFrame frame = frames.get(index % frames.size());

        if (item != null) {
            item.remove();
        }

        item = new Entity(EntityType.ITEM);
        ItemEntityMeta meta = (ItemEntityMeta)item.getEntityMeta();
        meta.setItem(frame.getVisual());

        item.setInstance(shop.instance(), shop.center().add(0, data.itemOffset, 0));

        TagResolver[] tags = getTagsForFrame(frame);

        List<Component> newComponents = new ArrayList<>(frame.hologramFormats().size());
        for (String formatString : frame.hologramFormats()) {
            newComponents.add(MiniMessage.miniMessage().deserialize(formatString, tags));
        }

        updateHologram(newComponents);
    }

    private TagResolver[] getTagsForFrame(SlotMachineFrame frame, TagResolver... additionalTags) {
        TagResolver rollingPlayerTag = Placeholder.component("rolling_player",
                rollInteraction.player().module().getPlayerView().getDisplayNameIfPresent());

        Component displayName = frame.getVisual().getDisplayName();
        if (displayName == null) {
            displayName = Component.translatable(frame.getVisual().material().registry().translationKey());
        }

        TagResolver itemName = Placeholder.component("item_name", displayName);

        TagResolver[] tags = new TagResolver[additionalTags.length + 2];
        tags[0] = rollingPlayerTag;
        tags[1] = itemName;

        System.arraycopy(additionalTags, 0, tags, 2, additionalTags.length);
        return tags;
    }

    private void updateHologram(List<Component> newComponents) {
        while (hologram.size() > newComponents.size()) {
            hologram.remove(hologram.size() - 1);
        }

        for (int i = 0; i < newComponents.size(); i++) {
            Component newComponent = newComponents.get(i);
            Component oldComponent = i < hologram.size() ? hologram.get(i) : null;

            if (!newComponent.equals(oldComponent)) {
                hologram.set(i, newComponent);
            }
        }
    }

    public interface SlotMachineFrame {
        @NotNull ItemStack getVisual();

        @NotNull List<ShopInteractor> interactors();

        @NotNull List<String> hologramFormats();
    }

    public interface DelayFormula {
        int delay(int frameCount, int frame);
    }

    @Model("zombies.map.shop.interactor.slot_machine.delay.constant")
    @Cache(false)
    public static class ConstantDelayFormula implements DelayFormula {
        private final Data data;

        @FactoryMethod
        public ConstantDelayFormula(@NotNull Data data) {
            this.data = data;
        }

        @Override
        public int delay(int frameCount, int frame) {
            return data.delay;
        }

        @DataObject
        public record Data(int delay) {
        }
    }

    @Model("zombies.map.shop.interactor.slot_machine.delay.linear")
    @Cache(false)
    public static class LinearDelayFormula implements DelayFormula {
        private final Data data;

        @FactoryMethod
        public LinearDelayFormula(@NotNull Data data) {
            this.data = data;
        }

        @Override
        public int delay(int frameCount, int frame) {
            return (int)Math.rint(
                    ((double)(data.endDelay - data.startDelay) / (double)frameCount) * frame + data.startDelay);
        }

        @DataObject
        public record Data(int startDelay, int endDelay) {
        }
    }

    @Model("zombies.map.shop.interactor.slot_machine.frame")
    @Cache(false)
    public static class BasicSlotMachineFrame implements SlotMachineFrame {
        private final Data data;
        private final List<ShopInteractor> interactors;

        @FactoryMethod
        public BasicSlotMachineFrame(@NotNull Data data,
                @NotNull @Child("interactors") List<ShopInteractor> interactors) {
            this.data = data;
            this.interactors = interactors;
        }

        @Override
        public @NotNull ItemStack getVisual() {
            return data.itemStack;
        }

        @Override
        public @NotNull List<ShopInteractor> interactors() {
            return interactors;
        }

        @Override
        public @NotNull List<String> hologramFormats() {
            return data.hologramFormats;
        }

        @DataObject
        public record Data(@NotNull ItemStack itemStack,
                           @NotNull List<String> hologramFormats,
                           @NotNull @ChildPath("interactors") List<String> interactors) {
        }
    }

    @DataObject
    public record Data(int frameCount,
                       double hologramOffset,
                       double itemOffset,
                       int gracePeriodTicks,
                       @NotNull List<String> gracePeriodFormats,
                       @NotNull @ChildPath("tick_formatter") String tickFormatter,
                       @NotNull @ChildPath("delay_formula") String delayFormula,
                       @NotNull @ChildPath("frames") List<String> frames,
                       @NotNull @ChildPath("roll_start_interactors") List<String> rollStartInteractors,
                       @NotNull @ChildPath("mismatched_player_interactors") List<String> mismatchedPlayerInteractors,
                       @NotNull @ChildPath("while_rolling_interactors") List<String> whileRollingInteractors,
                       @NotNull @ChildPath("timeout_expired_interactors") List<String> timeoutExpiredInteractors) {
    }
}
