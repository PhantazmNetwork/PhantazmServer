package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.api.player.PlayerView;
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

public class ShootExpEffect implements GunEffect {

    public record Data(@NotNull Key statsKey) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.effect.exp.shoot");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<Key> keyProcessor = AdventureConfigProcessors.key();

        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Key gunStatsKey = keyProcessor.dataFromElement(element.getElementOrThrow("gunStatsKey"));
                return new Data(gunStatsKey);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(1);
                node.put("gunStatsKey", keyProcessor.elementFromData(data.statsKey()));
                return node;
            }
        };
    }

    public static @NotNull BiConsumer<Data, Collection<Key>> dependencyConsumer() {
        return (data, keys) -> {
            keys.add(data.statsKey());
        };
    }

    private boolean currentlyActive = false;

    private final Data data;

    private final PlayerView playerView;

    private final GunStats stats;

    public ShootExpEffect(@NotNull Data data, @NotNull PlayerView playerView, @NotNull GunStats stats) {
        this.data = Objects.requireNonNull(data, "data");
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.stats = Objects.requireNonNull(stats, "stats");
    }

    @Override
    public void apply(@NotNull GunState state) {
        if (state.isMainEquipment()) {
            float exp = state.ammo() > 0 ? (float) state.ticksSinceLastShot() / stats.shootSpeed() : 0F; // TODO: fix for fire speed
            playerView.getPlayer().ifPresent(player -> player.setExp(exp));
            currentlyActive = true;
        }
        else if (currentlyActive) {
            playerView.getPlayer().ifPresent(player -> player.setExp(0));
            currentlyActive = false;
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
