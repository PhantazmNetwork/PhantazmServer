package com.github.phantazmnetwork.zombies.equipment.gun.reload;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.GunStats;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * A {@link ReloadTester} based solely on {@link GunState}.
 */
public class StateReloadTester implements ReloadTester {

    /**
     * Data for a {@link StateReloadTester}.
     * @param statsKey A {@link Key} to the gun's {@link GunStats}
     */
    public record Data(@NotNull Key statsKey) implements Keyed {

        /**
         * The serial {@link Key} for this {@link Data}.
         */
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.reload_tester.state");

        /**
         * Creates a {@link Data}.
         * @param statsKey A {@link Key} to the gun's {@link GunStats}
         */
        public Data {
            Objects.requireNonNull(statsKey, "statsKey");
        }

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<Key> keyProcessor = AdventureConfigProcessors.key();

        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Key statsKey = keyProcessor.dataFromElement(element.getElementOrThrow("statsKey"));
                return new Data(statsKey);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(1);
                node.put("statsKey", keyProcessor.elementFromData(data.statsKey()));

                return node;
            }
        };
    }

    /**
     * Creates a dependency consumer for {@link Data}s.
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    public static @NotNull BiConsumer<Data, Collection<Key>> dependencyConsumer() {
        return (data, keys) -> keys.add(data.statsKey());
    }

    private final GunStats stats;

    /**
     * Creates a {@link StateReloadTester}.
     * @param stats The gun's {@link GunStats}
     */
    public StateReloadTester(@NotNull GunStats stats) {
        this.stats = Objects.requireNonNull(stats, "stats");
    }

    @Override
    public boolean shouldReload(@NotNull GunState state) {
        return canReload(state) && state.clip() != stats.maxClip() && state.clip() != state.ammo();
    }

    @Override
    public boolean canReload(@NotNull GunState state) {
        return !isReloading(state) && state.ammo() > 0;
    }

    @Override
    public boolean isReloading(@NotNull GunState state) {
        return state.ticksSinceLastReload() < stats.reloadSpeed();
    }

}
