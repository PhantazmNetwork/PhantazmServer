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

import java.util.Objects;
import java.util.Set;

public class StateReloadTester implements ReloadTester {

    public record Data(@NotNull Key statsKey) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.reload_tester.state");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor(@NotNull Collection<Key> requested) {
        Objects.requireNonNull(requested, "requested");

        ConfigProcessor<Key> keyProcessor = AdventureConfigProcessors.key();
        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Key statsKey = keyProcessor.dataFromElement(element.getElementOrThrow("statsKey"));
                requested.add(statsKey);

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

    private final Data data;

    private final GunStats stats;

    public StateReloadTester(@NotNull Data data, @NotNull GunStats stats) {
        this.data = Objects.requireNonNull(data, "data");
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

    @Override
    public @NotNull Keyed getData() {
        return data;
    }

}
