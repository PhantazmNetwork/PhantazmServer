package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import net.kyori.adventure.key.Keyed;
import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunStats;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.ReloadTester;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.actionbar.ReloadActionBarChooser;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ReloadActionBarEffect implements GunEffect {

    public record Data(@NotNull Key statsKey,
                       @NotNull Key reloadTesterKey,
                       @NotNull Key reloadActionBarChooserKey) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.effect.action_bar.reload");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    private final Data data;

    private final PlayerView playerView;

    private final GunStats stats;

    private final ReloadTester reloadTester;

    private final ReloadActionBarChooser chooser;

    private boolean active = false;

    public ReloadActionBarEffect(@NotNull Data data, @NotNull PlayerView playerView, @NotNull GunStats stats,
                                 @NotNull ReloadTester reloadTester, @NotNull ReloadActionBarChooser chooser) {
        this.data = Objects.requireNonNull(data, "data");
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.stats = Objects.requireNonNull(stats, "stats");
        this.reloadTester = Objects.requireNonNull(reloadTester, "reloadTester");
        this.chooser = Objects.requireNonNull(chooser, "chooser");
    }

    @Override
    public void apply(@NotNull GunState state) {
        if (reloadTester.isReloading(state)) {
            if (state.isMainEquipment()) {
                float progress = (float) state.ticksSinceLastReload() / stats.reloadSpeed();
                playerView.getPlayer().ifPresent(player -> {
                    player.sendActionBar(chooser.choose(state, player, progress));
                });
            }
            active = true;
        } else if (active) {
            if (state.isMainEquipment()) {
                playerView.getPlayer().ifPresent(player -> {
                    player.sendActionBar(Component.empty());
                });
            }
            active = false;
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
