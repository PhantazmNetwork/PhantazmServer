package com.github.phantazmnetwork.zombies.game.map.shop.interactor;

import com.github.phantazmnetwork.commons.Prioritized;
import com.github.phantazmnetwork.commons.config.PrioritizedProcessor;
import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("zombies.map.shop.interactor.delayed")
public class DelayedInteractor extends InteractorBase<DelayedInteractor.Data> {
    private final ShopInteractor target;
    private PlayerInteraction interaction;
    private long startTime;

    @ProcessorMethod
    public static ConfigProcessor<Data> processor() {
        return new PrioritizedProcessor<>() {
            @Override
            public @NotNull Data finishData(@NotNull ConfigNode node, int priority) throws ConfigProcessException {
                String targetPath = node.getStringOrThrow("targetPath");
                int delayTicks = node.getNumberOrThrow("delayTicks").intValue();
                boolean resetOnInteract = node.getBooleanOrThrow("resetOnInteract");
                return new Data(priority, targetPath, delayTicks, resetOnInteract);
            }

            @Override
            public @NotNull ConfigNode finishNode(@NotNull Data data) {
                ConfigNode node = new LinkedConfigNode(3);
                node.putString("targetPath", data.targetPath);
                node.putNumber("delayTicks", data.delayTicks);
                node.putBoolean("resetOnInteract", data.resetOnInteract);
                return node;
            }
        };
    }

    @FactoryMethod
    public DelayedInteractor(@NotNull Data data, @NotNull @Dependency("zombies.dependency.map") ZombiesMap map,
            @DataName("target") ShopInteractor target) {
        super(data, map);
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
    record Data(int priority, @DataPath("target") String targetPath, int delayTicks, boolean resetOnInteract)
            implements Prioritized {

    }
}
