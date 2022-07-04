package com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunHit;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunShot;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class IgniteShotHandler implements ShotHandler {

    public record Data(int duration, int headshotDuration) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.hit_handler.ignite");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    private final Data data;

    public IgniteShotHandler(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public void handle(@NotNull GunState state, @NotNull Player attacker, @NotNull Collection<PhantazmMob> previousHits, @NotNull GunShot shot) {
        for (GunHit target : shot.regularTargets()) {
            target.mob().entity().setFireForDuration(data.duration());
        }
        for (GunHit target : shot.headshotTargets()) {
            target.mob().entity().setFireForDuration(data.headshotDuration());
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    @Override
    public @NotNull Keyed getData() {
        return data;
    }

}
