package com.github.phantazmnetwork.mob.target;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * A {@link TargetSelector} that selects nearby players.
 */
@Model("mob.selector.nearest_players")
public class NearestPlayersSelector extends NearestEntitiesSelector<Player> {

    @DataObject
    public record Data(double range, int targetLimit) {

    }

    /**
     * Creates a {@link NearestPlayersSelector}.
     */
    @FactoryMethod
    public NearestPlayersSelector(@NotNull Data data, @NotNull @Dependency("mob.entity.entity") Entity entity) {
        super(entity, data.range(), data.targetLimit());
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                double range = element.getNumberOrThrow("range").doubleValue();
                int targetLimit = element.getNumberOrThrow("targetLimit").intValue();

                return new Data(range, targetLimit);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) {
                return ConfigNode.of("range", data.range(), "targetLimit", data.targetLimit());
            }
        };
    }

    @Override
    protected @NotNull Optional<Player> mapTarget(@NotNull Entity entity) {
        if (entity instanceof Player player) {
            return Optional.of(player);
        }

        return Optional.empty();
    }

    @Override
    protected boolean isTargetValid(@NotNull Entity targetEntity, @NotNull Player target) {
        return true;
    }

}
