package com.github.phantazmnetwork.mob.target;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.Model;
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
    public NearestPlayersSelector(@NotNull Data data, @NotNull @Dependency("mob.entity.entity") Entity entity) {
        super(entity, data.range(), data.targetLimit());
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
