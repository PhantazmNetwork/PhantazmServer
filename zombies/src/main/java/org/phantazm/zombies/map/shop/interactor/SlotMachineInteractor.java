package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
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
import org.jetbrains.annotations.Nullable;
import org.phantazm.core.hologram.Hologram;
import org.phantazm.core.hologram.InstanceHologram;
import org.phantazm.core.time.TickFormatter;
import org.phantazm.zombies.map.Evaluation;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.map.shop.predicate.ShopPredicate;

import java.util.*;

@Model("zombies.map.shop.interactor.slot_machine")
@Cache(false)
public class SlotMachineInteractor implements ShopInteractor {
    private final Data data;
    private final List<ShopPredicate> rollPredicates;
    private final TickFormatter tickFormatter;
    private final DelayFormula delayFormula;
    private final List<SlotMachineFrame> frames;
    private final List<ShopInteractor> rollFailInteractors;
    private final List<ShopInteractor> rollStartInteractors;
    private final List<ShopInteractor> mismatchedPlayerInteractors;
    private final List<ShopInteractor> whileRollingInteractors;
    private final List<ShopInteractor> timeoutExpiredInteractors;
    private final List<ShopInteractor> itemClaimedInteractors;
    private final List<ShopInteractor> endInteractors;
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
    public SlotMachineInteractor(Data data, @NotNull @Child("roll_predicates") List<ShopPredicate> rollPredicates,
            @NotNull @Child("tick_formatter") TickFormatter tickFormatter,
            @NotNull @Child("delay_formula") DelayFormula delayFormula,
            @NotNull @Child("frames") List<SlotMachineFrame> frames,
            @NotNull @Child("roll_fail_interactors") List<ShopInteractor> rollFailInteractors,
            @NotNull @Child("roll_start_interactors") List<ShopInteractor> rollStartInteractors,
            @NotNull @Child("mismatched_player_interactors") List<ShopInteractor> mismatchedPlayerInteractors,
            @NotNull @Child("while_rolling_interactors") List<ShopInteractor> whileRollingInteractors,
            @NotNull @Child("timeout_expired_interactors") List<ShopInteractor> timeoutExpiredInteractors,
            @NotNull @Child("item_claimed_interactors") List<ShopInteractor> itemClaimedInteractors,
            @NotNull @Child("end_interactors") List<ShopInteractor> endInteractors, @NotNull Random random) {
        this.data = data;
        this.rollPredicates = rollPredicates;
        this.tickFormatter = tickFormatter;
        this.delayFormula = delayFormula;
        this.frames = new ArrayList<>(frames);
        this.rollFailInteractors = rollFailInteractors;
        this.rollStartInteractors = rollStartInteractors;
        this.mismatchedPlayerInteractors = mismatchedPlayerInteractors;
        this.whileRollingInteractors = whileRollingInteractors;
        this.timeoutExpiredInteractors = timeoutExpiredInteractors;
        this.itemClaimedInteractors = itemClaimedInteractors;
        this.endInteractors = endInteractors;
        this.random = random;
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        this.shop = shop;
        this.hologram = new InstanceHologram(shop.center().add(0, data.hologramOffset, 0), 0);
        this.hologram.setInstance(shop.instance());

        ShopInteractor.initialize(rollStartInteractors, shop);
        ShopInteractor.initialize(mismatchedPlayerInteractors, shop);
        ShopInteractor.initialize(whileRollingInteractors, shop);
        ShopInteractor.initialize(timeoutExpiredInteractors, shop);
        ShopInteractor.initialize(itemClaimedInteractors, shop);
        ShopInteractor.initialize(endInteractors, shop);

        for (SlotMachineFrame frame : frames) {
            ShopInteractor.initialize(frame.interactors(), shop);
        }
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        if (rolling) {
            if (interaction.player() != rollInteraction.player()) {
                ShopInteractor.handle(mismatchedPlayerInteractors, interaction);
                return false;
            }

            if (!doneRolling) {
                ShopInteractor.handle(whileRollingInteractors, interaction);
                return false;
            }

            if (!frames.isEmpty()) {
                SlotMachineFrame currentFrame = frames.get((currentFrameIndex - 1) % frames.size());
                if (!ShopInteractor.handle(currentFrame.interactors(), rollInteraction)) {
                    return false;
                }

                rollInteraction.player().getPlayer().ifPresent(player -> player.sendMessage(MiniMessage.miniMessage()
                        .deserialize(data.itemClaimedFormat,
                                Placeholder.component("claimed_item", getItemName(currentFrame.getVisual())))));
            }

            ShopInteractor.handle(itemClaimedInteractors, rollInteraction);
            reset();
            return true;
        }

        if (!data.evaluation.evaluate(rollPredicates, interaction, shop)) {
            ShopInteractor.handle(rollFailInteractors, interaction);
            return false;
        }

        rolling = true;
        shop.flags().setFlag(data.rollingFlag);

        Collections.shuffle(frames, random);

        rollInteraction = interaction;
        lastFrameTime = System.currentTimeMillis();
        currentFrameIndex = 0;
        ticksUntilNextFrame = delayFormula.delay(data.frameCount, currentFrameIndex);

        ShopInteractor.handle(rollStartInteractors, interaction);
        return true;
    }

    @Override
    public void tick(long time) {
        ShopInteractor.tick(rollStartInteractors, time);
        ShopInteractor.tick(mismatchedPlayerInteractors, time);
        ShopInteractor.tick(whileRollingInteractors, time);
        ShopInteractor.tick(timeoutExpiredInteractors, time);
        ShopInteractor.tick(itemClaimedInteractors, time);
        ShopInteractor.tick(endInteractors, time);

        for (SlotMachineFrame frame : frames) {
            ShopInteractor.tick(frame.interactors(), time);
        }

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

        if (currentFrameIndex < data.frameCount) {
            return;
        }

        if (!doneRolling) {
            rollFinishTime = time;

            if (!frames.isEmpty()) {
                SlotMachineFrame frame = frames.get((currentFrameIndex - 1) % frames.size());
                Component component = MiniMessage.miniMessage().deserialize(data.itemRolledFormat,
                        Placeholder.component("rolled_item", getItemName(frame.getVisual())));

                if (!rollInteraction.player().hasQuit()) {
                    rollInteraction.player().getPlayer().ifPresent(player -> player.sendMessage(component));
                }
            }
        }

        doneRolling = true;
        long ticksSinceDoneRolling = (time - rollFinishTime) / MinecraftServer.TICK_MS;

        if (ticksSinceDoneRolling < data.gracePeriodTicks) {
            String timeString = tickFormatter.format(data.gracePeriodTicks - ticksSinceDoneRolling);
            TagResolver[] tags =
                    getTagsForFrame(frames.isEmpty() ? null : frames.get((currentFrameIndex - 1) % frames.size()),
                            Placeholder.unparsed("time_left", timeString));

            List<Component> newComponents = new ArrayList<>(data.gracePeriodFormats.size());
            for (String formatString : data.gracePeriodFormats) {
                newComponents.add(MiniMessage.miniMessage().deserialize(formatString, tags));
            }

            updateHologram(newComponents);
            return;
        }

        if (!rollInteraction.player().hasQuit()) {
            ShopInteractor.handle(timeoutExpiredInteractors, rollInteraction);
        }

        reset();
    }

    private void reset() {
        ShopInteractor.handle(endInteractors, rollInteraction);

        if (item != null) {
            item.remove();
            item = null;
        }

        hologram.clear();
        rollInteraction = null;
        rolling = false;
        shop.flags().clearFlag(data.rollingFlag);
        doneRolling = false;
        rollFinishTime = 0L;
        lastFrameTime = 0L;
        currentFrameIndex = 0;
        ticksUntilNextFrame = 0;
    }

    private void displayRollFrame(int index) {
        if (frames.isEmpty()) {
            return;
        }

        SlotMachineFrame frame = frames.get(index % frames.size());

        if (item != null) {
            item.remove();
        }

        item = new Entity(EntityType.ITEM);
        ItemEntityMeta meta = (ItemEntityMeta)item.getEntityMeta();
        meta.setItem(frame.getVisual());
        meta.setHasNoGravity(true);

        item.setInstance(shop.instance(), shop.center().add(0, data.itemOffset, 0));

        TagResolver[] tags = getTagsForFrame(frame);
        List<Component> newComponents = new ArrayList<>(data.frameHologramFormats.size());
        for (String formatString : data.frameHologramFormats) {
            newComponents.add(MiniMessage.miniMessage().deserialize(formatString, tags));
        }

        updateHologram(newComponents);
    }

    private TagResolver[] getTagsForFrame(@Nullable SlotMachineFrame frame, TagResolver... additionalTags) {
        TagResolver rollingPlayerTag = Placeholder.component("rolling_player",
                rollInteraction.player().module().getPlayerView().getDisplayNameIfPresent());

        Component displayName = frame != null ? getItemName(frame.getVisual()) : Component.empty();
        TagResolver itemName = Placeholder.component("item_name", displayName);

        TagResolver[] tags = new TagResolver[additionalTags.length + 2];
        tags[0] = rollingPlayerTag;
        tags[1] = itemName;

        System.arraycopy(additionalTags, 0, tags, 2, additionalTags.length);
        return tags;
    }

    private Component getItemName(ItemStack itemStack) {
        Component displayName = itemStack.getDisplayName();
        if (displayName == null) {
            displayName = Component.translatable(itemStack.material().registry().translationKey());
        }

        return displayName;
    }

    private void updateHologram(List<Component> newComponents) {
        while (hologram.size() > newComponents.size()) {
            hologram.remove(hologram.size() - 1);
        }

        for (int i = 0; i < newComponents.size(); i++) {
            Component newComponent = newComponents.get(i);
            Component oldComponent = i < hologram.size() ? hologram.get(i) : null;

            if (!newComponent.equals(oldComponent)) {
                if (oldComponent == null) {
                    hologram.add(newComponent);
                    continue;
                }

                hologram.set(i, newComponent);
            }
        }
    }

    public interface SlotMachineFrame {
        @NotNull ItemStack getVisual();

        @NotNull List<ShopInteractor> interactors();
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

        @DataObject
        public record Data(@NotNull ItemStack itemStack, @NotNull @ChildPath("interactors") List<String> interactors) {
        }
    }

    @DataObject
    public record Data(int frameCount,
                       double hologramOffset,
                       double itemOffset,
                       int gracePeriodTicks,
                       @NotNull String itemRolledFormat,
                       @NotNull String itemClaimedFormat,
                       @NotNull List<String> frameHologramFormats,
                       @NotNull Key rollingFlag,
                       @NotNull List<String> gracePeriodFormats,
                       @NotNull Evaluation evaluation,
                       @NotNull @ChildPath("roll_predicates") List<String> rollPredicates,
                       @NotNull @ChildPath("tick_formatter") String tickFormatter,
                       @NotNull @ChildPath("delay_formula") String delayFormula,
                       @NotNull @ChildPath("frames") List<String> frames,
                       @NotNull @ChildPath("roll_fail_interactors") List<String> rollFailInteractors,
                       @NotNull @ChildPath("roll_start_interactors") List<String> rollStartInteractors,
                       @NotNull @ChildPath("mismatched_player_interactors") List<String> mismatchedPlayerInteractors,
                       @NotNull @ChildPath("while_rolling_interactors") List<String> whileRollingInteractors,
                       @NotNull @ChildPath("timeout_expired_interactors") List<String> timeoutExpiredInteractors,
                       @NotNull @ChildPath("item_claimed_interactors") List<String> itemClaimedInteractors,
                       @NotNull @ChildPath("end_interactors") List<String> endInteractors) {
    }
}
