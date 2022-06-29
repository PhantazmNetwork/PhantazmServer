package com.github.phantazmnetwork.mob.target;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.PhantazmMob;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class NearestPlayersSelector extends NearestEntitiesSelector<Player> {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "selector.nearest_players");

    public NearestPlayersSelector(double range, int targetLimit) {
        super(range, targetLimit);
    }

    @Override
    protected @NotNull Optional<Player> mapTarget(@NotNull Entity entity) {
        if (entity instanceof Player player) {
            return Optional.of(player);
        }

        return Optional.empty();
    }

    @Override
    protected boolean isTargetValid(@NotNull PhantazmMob mob, @NotNull Entity targetEntity, @NotNull Player target) {
        return true;
    }

    @Override
    public @NotNull Key key() {
        return SERIAL_KEY;
    }
}
