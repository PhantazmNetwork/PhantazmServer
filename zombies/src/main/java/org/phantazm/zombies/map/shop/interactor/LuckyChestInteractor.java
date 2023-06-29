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
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.ArrayList;
import java.util.List;

@Model("zombies.map.shop.interactor.lucky_chest")
@Cache(false)
public class LuckyChestInteractor implements ShopInteractor {
    private final Data data;
    private final DelayFormula delayFormula;
    private final List<LuckyChestFrame> frames;
    private final List<ShopInteractor> whileRollingInteractors;

    //non-null in tick and interaction methods - set in initialize(Shop)
    private Shop shop;
    private Hologram hologram;


    private Entity item;

    private ZombiesPlayer rollingPlayer;
    private boolean rolling;
    private boolean doneRolling;

    private long rollFinishTime;

    private long lastFrameTime;
    private int currentFrameIndex;
    private int ticksUntilNextFrame;
    private LuckyChestFrame nextFrame;

    @FactoryMethod
    public LuckyChestInteractor(Data data, @NotNull @Child("delay_formula") DelayFormula delayFormula,
            @NotNull @Child("frames") List<LuckyChestFrame> frames,
            @NotNull @Child("while_rolling_interactors") List<ShopInteractor> whileRollingInteractors) {
        this.data = data;
        this.delayFormula = delayFormula;
        this.frames = frames;
        this.whileRollingInteractors = whileRollingInteractors;
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
            if (!doneRolling) {
                for (ShopInteractor interactor : whileRollingInteractors) {
                    interactor.handleInteraction(interaction);
                }

                return false;
            }

            return false;
        }

        rolling = true;

        rollingPlayer = interaction.player();
        lastFrameTime = System.currentTimeMillis();
        currentFrameIndex = 0;
        ticksUntilNextFrame = delayFormula.delay(data.frameCount, currentFrameIndex);
        nextFrame = frames.get(0);
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
                displayFrame(currentFrameIndex);

                lastFrameTime = time;
                currentFrameIndex++;
                ticksUntilNextFrame = delayFormula.delay(data.frameCount, currentFrameIndex);
                nextFrame = frames.get(currentFrameIndex % frames.size());
            }
        }

        if (currentFrameIndex == data.frameCount) {
            if (!doneRolling) {
                rollFinishTime = time;
            }

            doneRolling = true;


        }
    }

    private void displayFrame(int index) {
        LuckyChestFrame frame = frames.get(index);

        if (item != null) {
            item.remove();
        }

        item = new Entity(EntityType.ITEM);
        ItemEntityMeta meta = (ItemEntityMeta)item.getEntityMeta();
        meta.setItem(frame.getVisual());

        item.setInstance(shop.instance(), shop.center().add(0, data.itemOffset, 0));

        rollingPlayer.getUsername();
        TagResolver rollingPlayerTag = Placeholder.component("rolling_player",
                rollingPlayer.module().getPlayerView().getDisplayNameIfPresent());

        Component displayName = frame.getVisual().getDisplayName();
        if (displayName == null) {
            displayName = Component.text(frame.getVisual().material().key().value());
        }

        TagResolver itemName = Placeholder.component("item_name", displayName);

        List<Component> newComponents = new ArrayList<>(frame.hologramFormats().size());
        for (String formatString : frame.hologramFormats()) {
            newComponents.add(MiniMessage.miniMessage().deserialize(formatString, rollingPlayerTag, itemName));
        }

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

    public interface LuckyChestFrame {
        @NotNull ItemStack getVisual();

        @NotNull ShopInteractor interactor();

        @NotNull List<String> hologramFormats();
    }

    public interface DelayFormula {
        int delay(int frameCount, int frame);
    }

    @Model("zombies.map.shop.interactor.lucky_chest.delay.constant")
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

    @Model("zombies.map.shop.interactor.lucky_chest.delay.linear")
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

    @Model("zombies.map.shop.interactor.lucky_chest.frame")
    @Cache(false)
    public static class BasicLuckyChestFrame implements LuckyChestFrame {
        private final Data data;
        private final ShopInteractor interactor;

        @FactoryMethod
        public BasicLuckyChestFrame(@NotNull Data data, @NotNull ShopInteractor interactor) {
            this.data = data;
            this.interactor = interactor;
        }

        @Override
        public @NotNull ItemStack getVisual() {
            return data.itemStack;
        }

        @Override
        public @NotNull ShopInteractor interactor() {
            return interactor;
        }

        @Override
        public @NotNull List<String> hologramFormats() {
            return data.hologramFormats;
        }

        public record Data(@NotNull ItemStack itemStack,
                           @NotNull List<String> hologramFormats,
                           @NotNull @ChildPath("interactor") String interactor) {
        }
    }

    @DataObject
    public record Data(int frameCount,
                       double hologramOffset,
                       double itemOffset,
                       int gracePeriodTicks,
                       @NotNull @ChildPath("delay_formula") String delayFormula,
                       @NotNull @ChildPath("frames") List<String> frames,
                       @NotNull @ChildPath("while_rolling_interactors") List<String> whileRollingInteractors) {
    }
}
