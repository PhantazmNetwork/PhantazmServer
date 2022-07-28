package com.github.phantazmnetwork.zombies.equipment.gun.target.tester;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.mob.PhantazmMob;
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
 * A {@link TargetTester} that only selects {@link PhantazmMob}s.
 */
public class PhantazmMobTargetTester implements TargetTester {

    private final Data data;
    private final MobStore mobStore;

    /**
     * Creates a {@link PhantazmMobTargetTester}.
     *
     * @param data     The {@link PhantazmMobTargetTester}'s {@link Data}
     * @param mobStore The {@link MobStore} to retrieve {@link PhantazmMob}s from
     */
    public PhantazmMobTargetTester(@NotNull Data data, @NotNull MobStore mobStore) {
        this.data = Objects.requireNonNull(data, "data");
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
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
                boolean ignorePreviousHits = element.getBooleanOrThrow("ignorePreviousHits");

                return new Data(ignorePreviousHits);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) {
                ConfigNode node = new LinkedConfigNode(1);
                node.putBoolean("ignorePreviousHits", data.ignorePreviousHits());

                return node;
            }
        };
    }

    @Override
    public boolean useTarget(@NotNull Entity target, @NotNull Collection<UUID> previousHits) {
        UUID uuid = target.getUuid();
        return !(data.ignorePreviousHits() && previousHits.contains(target.getUuid())) && mobStore.getMob(uuid) != null;
    }

    /**
     * Data for a {@link PhantazmMobTargetTester}.
     *
     * @param ignorePreviousHits Whether to ignore previously hit {@link UUID}s
     */
    public record Data(boolean ignorePreviousHits) implements Keyed {

        /**
         * The serial {@link Key} of this {@link Data}.
         */
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.target_tester.phantazm");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }

    }
}
