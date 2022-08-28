package com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire;

import com.github.phantazmnetwork.commons.ConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * A {@link Firer} which delegates to multiple sub-{@link Firer}.
 * Sub-{@link Firer}s may shoot at a slightly different angle than the direction of the original shot.
 */
public class SpreadFirer implements Firer {

    private final Data data;
    private final Random random;
    private final Collection<Firer> subFirers;

    /**
     * Creates a {@link SpreadFirer}.
     *
     * @param data      The {@link SpreadFirer}'s {@link Data}
     * @param random    The {@link Random} to use for angle variance
     * @param subFirers A {@link Collection} of sub-{@link Firer}s
     */
    public SpreadFirer(@NotNull Data data, @NotNull Random random, @NotNull Collection<Firer> subFirers) {
        this.data = Objects.requireNonNull(data, "data");
        this.random = Objects.requireNonNull(random, "random");
        this.subFirers = List.copyOf(subFirers);
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     *
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<Key> keyProcessor = ConfigProcessors.key();
        ConfigProcessor<Collection<Key>> collectionProcessor = keyProcessor.collectionProcessor();

        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Collection<Key> subFirerKeys =
                        collectionProcessor.dataFromElement(element.getElementOrThrow("subFirerKeys"));
                float angleVariance = element.getNumberOrThrow("angleVariance").floatValue();
                if (angleVariance < 0) {
                    throw new ConfigProcessException("angleVariance must be greater than or equal to 0");
                }

                return new Data(subFirerKeys, angleVariance);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(2);
                node.put("subFirerKeys", collectionProcessor.elementFromData(data.subFirerKeys()));
                node.putNumber("angleVariance", data.angleVariance());

                return node;
            }
        };
    }

    /**
     * Creates a dependency consumer for {@link Data}s.
     *
     * @return A dependency consumer for {@link Data}s
     */
    public static @NotNull BiConsumer<Data, Collection<Key>> dependencyConsumer() {
        return (data, keys) -> keys.addAll(data.subFirerKeys());
    }

    @Override
    public void fire(@NotNull GunState state, @NotNull Pos start, @NotNull Collection<UUID> previousHits) {
        if (data.angleVariance() == 0) {
            for (Firer subFirer : subFirers) {
                subFirer.fire(state, start, previousHits);
            }
            return;
        }

        Vec direction = start.direction();
        double yaw = Math.atan2(direction.z(), direction.x());
        double noYMagnitude = Math.sqrt(direction.x() * direction.x() + direction.z() * direction.z());
        double pitch = Math.atan2(direction.y(), noYMagnitude);

        for (Firer subFirer : subFirers) {
            double newYaw = yaw + data.angleVariance() * (2 * random.nextDouble() - 1);
            double newPitch = pitch + data.angleVariance() * (2 * random.nextDouble() - 1);

            Vec newDirection = new Vec(Math.cos(newYaw) * Math.cos(newPitch), Math.sin(newPitch),
                    Math.sin(newYaw) * Math.cos(newPitch));
            subFirer.fire(state, start.withDirection(newDirection), previousHits);
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {
        for (Firer firer : subFirers) {
            firer.tick(state, time);
        }
    }

    /**
     * Data for a {@link SpreadFirer}.
     *
     * @param subFirerKeys  A {@link Collection} of {@link Key}s to the {@link SpreadFirer}'s sub-{@link Firer}s
     * @param angleVariance The maximum angle variance for each sub-{@link Firer}
     */
    public record Data(@NotNull Collection<Key> subFirerKeys, float angleVariance) implements Keyed {

        /**
         * The serial {@link Key} of this {@link Data}.
         */
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.firer.spread");

        /**
         * Creates a {@link Data}.
         *
         * @param subFirerKeys  A {@link Collection} of {@link Key}s to the {@link SpreadFirer}'s sub-{@link Firer}s
         * @param angleVariance The maximum angle variance for each sub-{@link Firer}
         */
        public Data {
            Objects.requireNonNull(subFirerKeys, "subFirerKeys");
        }

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

}
