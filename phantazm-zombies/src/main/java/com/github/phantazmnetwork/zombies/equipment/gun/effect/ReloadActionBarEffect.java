package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.GunStats;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.ReloadTester;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.actionbar.ReloadActionBarChooser;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiConsumer;

public class ReloadActionBarEffect implements GunEffect {

    public record Data(@NotNull Key statsKey,
                       @NotNull Key reloadTesterKey,
                       @NotNull Key reloadActionBarChooserKey) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.effect.action_bar.reload");

        public Data {
            Objects.requireNonNull(statsKey, "statsKey");
            Objects.requireNonNull(reloadTesterKey, "reloadTesterKey");
            Objects.requireNonNull(reloadActionBarChooserKey, "reloadActionBarChooserKey");
        }

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
                Key statsKey = keyProcessor.dataFromElement(element.getElementOrThrow("statsKey"));
                Key reloadTesterKey = keyProcessor.dataFromElement(element.getElementOrThrow("reloadTesterKey"));
                Key reloadActionBarChooserKey = keyProcessor.dataFromElement(element.getElementOrThrow("reloadActionBarChooserKey"));

                return new Data(statsKey, reloadTesterKey, reloadActionBarChooserKey);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(3);
                node.put("statsKey", keyProcessor.elementFromData(data.statsKey()));
                node.put("reloadTesterKey", keyProcessor.elementFromData(data.reloadTesterKey()));
                node.put("reloadActionBarChooserKey", keyProcessor.elementFromData(data.reloadActionBarChooserKey()));

                return node;
            }
        };
    }

    public static @NotNull BiConsumer<Data, Collection<Key>> dependencyConsumer() {
        return (data, keys) -> {
            keys.add(data.statsKey());
            keys.add(data.reloadTesterKey());
            keys.add(data.reloadActionBarChooserKey());
        };
    }

    private final PlayerView playerView;

    private final GunStats stats;

    private final ReloadTester reloadTester;

    private final ReloadActionBarChooser chooser;

    private boolean active = false;

    public ReloadActionBarEffect(@NotNull PlayerView playerView, @NotNull GunStats stats,
                                 @NotNull ReloadTester reloadTester, @NotNull ReloadActionBarChooser chooser) {
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.stats = Objects.requireNonNull(stats, "stats");
        this.reloadTester = Objects.requireNonNull(reloadTester, "reloadTester");
        this.chooser = Objects.requireNonNull(chooser, "chooser");
    }

    @Override
    public void apply(@NotNull GunState state) {
        if (reloadTester.isReloading(state) && state.isMainEquipment()) {
            float progress = (float) state.ticksSinceLastReload() / stats.reloadSpeed();
            playerView.getPlayer().ifPresent(player -> player.sendActionBar(chooser.choose(state, player, progress)));
            active = true;
        } else if (active) {
            playerView.getPlayer().ifPresent(player -> player.sendActionBar(Component.empty()));
            active = false;
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

}
