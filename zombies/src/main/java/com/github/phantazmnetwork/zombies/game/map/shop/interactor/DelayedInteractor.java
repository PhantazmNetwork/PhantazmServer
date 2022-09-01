package com.github.phantazmnetwork.zombies.game.map.shop.interactor;

import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("zombies.map.shop.interactor.delayed")
@Cache(false)
public class DelayedInteractor extends InteractorBase<DelayedInteractor.Data> {
    private final ShopInteractor target;

    private PlayerInteraction interaction;
    private long startTime;

    @ProcessorMethod
    public static ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                String targetPath = element.getStringOrThrow("targetPath");
                int delayTicks = element.getNumberOrThrow("delayTicks").intValue();
                boolean resetOnInteract = element.getBooleanOrThrow("resetOnInteract");
                return new Data(targetPath, delayTicks, resetOnInteract);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) {
                ConfigNode node = new LinkedConfigNode(3);
                node.putString("targetPath", data.targetPath);
                node.putNumber("delayTicks", data.delayTicks);
                node.putBoolean("resetOnInteract", data.resetOnInteract);
                return node;
            }
        };
    }

    @FactoryMethod
    public DelayedInteractor(@NotNull Data data, @DataName("target") ShopInteractor target) {
        super(data);
        this.target = Objects.requireNonNull(target, "target");
    }

    @Override
    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        if (this.interaction == null || data.resetOnInteract) {
            this.startTime = System.currentTimeMillis();
            this.interaction = interaction;
        }
    }

    @Override
    public void tick(long time) {
        super.tick(time);

        if (interaction != null) {
            long elapsedTimeMs = time - startTime;
            int elapsedTicks = (int)(elapsedTimeMs / MinecraftServer.TICK_MS);

            if (elapsedTicks >= data.delayTicks) {
                target.handleInteraction(interaction);
                interaction = null;
            }
        }
    }

    @DataObject
    record Data(@DataPath("target") String targetPath, int delayTicks, boolean resetOnInteract) {

    }
}