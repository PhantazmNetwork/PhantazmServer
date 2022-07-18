package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.GunStats;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * A {@link GunEffect} that sets a {@link Player}'s exp based on the time since their last shot.
 */
public class ShootExpEffect implements GunEffect {

    private final PlayerView playerView;
    private final GunStats stats;
    private boolean currentlyActive = false;

    /**
     * Creates a {@link ShootExpEffect}.
     *
     * @param playerView The {@link PlayerView} of the {@link Player} to set the exp of
     * @param stats      The {@link GunStats} of the gun
     */
    public ShootExpEffect(@NotNull PlayerView playerView, @NotNull GunStats stats) {
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.stats = Objects.requireNonNull(stats, "stats");
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     *
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
     *
     * @return A dependency consumer for {@link Data}s
     */
    public static @NotNull BiConsumer<Data, Collection<Key>> dependencyConsumer() {
        return (data, keys) -> keys.add(data.statsKey());
    }

    @Override
    public void apply(@NotNull GunState state) {
        if (state.isMainEquipment()) {
            float exp = state.ammo() > 0
                        ? (float)state.ticksSinceLastShot() / stats.shootSpeed()
                        : 0F; // TODO: fix for fire speed
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

    /**
     * Data for a {@link ShootExpEffect}.
     *
     * @param statsKey A {@link Key} to the gun's {@link GunStats}
     */
    public record Data(@NotNull Key statsKey) implements Keyed {

        /**
         * The serial {@link Key} for this {@link Data}.
         */
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.effect.exp.shoot");

        /**
         * Creates a {@link Data}.
         *
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

}
