package com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunHit;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunShot;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * A {@link ShotHandler} that sets {@link Entity}s on fire.
 */
public class IgniteShotHandler implements ShotHandler {

    private final Data data;

    /**
     * Creates an {@link IgniteShotHandler}.
     *
     * @param data The {@link IgniteShotHandler}'s {@link Data}
     */
    public IgniteShotHandler(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     *
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                int duration = element.getNumberOrThrow("duration").intValue();
                if (duration < 0) {
                    throw new ConfigProcessException("duration must be greater than or equal to 0");
                }
                int headshotDuration = element.getNumberOrThrow("headshotDuration").intValue();
                if (headshotDuration < 0) {
                    throw new ConfigProcessException("headshotDuration must be greater than or equal to 0");
                }

                return new Data(duration, headshotDuration);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) {
                ConfigNode node = new LinkedConfigNode(2);
                node.putNumber("duration", data.duration());
                node.putNumber("headshotDuration", data.headshotDuration());
                return node;
            }
        };
    }

    @Override
    public void handle(@NotNull GunState state, @NotNull Entity attacker, @NotNull Collection<UUID> previousHits,
            @NotNull GunShot shot) {
        for (GunHit target : shot.regularTargets()) {
            target.entity().setFireForDuration(data.duration());
        }
        for (GunHit target : shot.headshotTargets()) {
            target.entity().setFireForDuration(data.headshotDuration());
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    /**
     * Data for an {@link IgniteShotHandler}.
     *
     * @param duration         The duration of the fire for regular targets
     * @param headshotDuration The duration of the fire for headshots
     */
    public record Data(int duration, int headshotDuration) implements Keyed {

        /**
         * The serial {@link Key} of this {@link Data}.
         */
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.shot_handler.ignite");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

}
