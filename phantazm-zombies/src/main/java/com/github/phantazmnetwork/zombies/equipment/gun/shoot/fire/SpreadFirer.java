package com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
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

public class SpreadFirer implements Firer {

    public record Data(@NotNull Collection<Key> subFirerKeys, float angleVariance) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM,"gun.firer.spread");

        public Data {
            Objects.requireNonNull(subFirerKeys, "subFirerKeys");
            if (angleVariance < 0) {
                throw new IllegalArgumentException("angleVariance must be greater than or equal to 0");
            }
        }

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<Key> keyProcessor = AdventureConfigProcessors.key();
        ConfigProcessor<Collection<Key>> collectionProcessor = keyProcessor.collectionProcessor(ArrayList::new);

        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Collection<Key> subFirerKeys = collectionProcessor.dataFromElement(element.getElementOrThrow("subFirerKeys"));
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

    public static @NotNull BiConsumer<Data, Collection<Key>> dependencyConsumer() {
        return (data, keys) -> keys.addAll(data.subFirerKeys());
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

}
