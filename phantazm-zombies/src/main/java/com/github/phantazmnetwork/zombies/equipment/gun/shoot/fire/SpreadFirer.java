package com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.Random;

public class SpreadFirer implements Firer {

    public record Data(@NotNull Collection<Key> subFirerKeys, float angleVariance) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM,"gun.firer.spread");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    private final Data data;

    private final Random random;

    private final Collection<Firer> subFirers;

    public SpreadFirer(@NotNull Data data, @NotNull Random random, @NotNull Collection<Firer> subFirers) {
        this.data = Objects.requireNonNull(data, "data");
        this.random = Objects.requireNonNull(random, "random");
        this.subFirers = Objects.requireNonNull(subFirers, "subFirers");
    }

    @Override
    public void fire(@NotNull GunState state, @NotNull Pos start, @NotNull Collection<PhantazmMob> previousHits) {
        for (Firer subFirer : subFirers) {
            if (data.angleVariance() == 0) {
                subFirer.fire(state, start, previousHits);
            }
            else {
                Vec direction = start.direction();
                double yaw = Math.atan2(direction.z(), direction.x());
                double noYMagnitude = Math.sqrt(direction.x() * direction.x() + direction.z() * direction.z());
                double pitch = Math.atan2(direction.y(), noYMagnitude);

                yaw += data.angleVariance() * (2 * random.nextDouble() - 1);
                pitch += data.angleVariance() * (2 * random.nextDouble() - 1);

                Vec newDirection = new Vec(Math.cos(yaw) * Math.cos(pitch), Math.sin(pitch),
                        Math.sin(yaw) * Math.cos(pitch));
                subFirer.fire(state, start.withDirection(newDirection), previousHits);
            }
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {
        for (Firer firer : subFirers) {
            firer.tick(state, time);
        }
    }

    @Override
    public @NotNull Keyed getData() {
        return data;
    }
}
