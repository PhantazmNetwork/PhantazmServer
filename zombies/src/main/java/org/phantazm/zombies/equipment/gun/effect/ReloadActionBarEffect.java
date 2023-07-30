package org.phantazm.zombies.equipment.gun.effect;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.GunStats;
import org.phantazm.zombies.equipment.gun.action_bar.ActionBarSender;
import org.phantazm.zombies.equipment.gun.reload.ReloadTester;
import org.phantazm.zombies.equipment.gun.reload.actionbar.ReloadActionBarChooser;

import java.util.Objects;

/**
 * A {@link GunEffect} that sends a message to an {@link Audience}'s action bar.
 */
@Model("zombies.gun.effect.reload.action_bar")
@Cache(false)
public class ReloadActionBarEffect implements GunEffect {

    private final GunStats stats;
    private final ActionBarSender actionBarSender;
    private final ReloadTester reloadTester;
    private final ReloadActionBarChooser chooser;
    private boolean active = false;

    /**
     * Creates a {@link ReloadActionBarEffect}.
     *
     * @param stats            The gun's {@link GunStats}
     * @param reloadTester     The gun's {@link ReloadTester}
     * @param chooser          The {@link ReloadActionBarChooser} to choose an action bar to send to the {@link Audience}
     */
    @FactoryMethod
    public ReloadActionBarEffect(@NotNull @Child("stats") GunStats stats,
            @NotNull @Child("action_bar_sender") ActionBarSender actionBarSender,
            @NotNull @Child("reload_tester") ReloadTester reloadTester,
            @NotNull @Child("reload_action_bar_chooser") ReloadActionBarChooser chooser) {
        this.stats = Objects.requireNonNull(stats, "stats");
        this.actionBarSender = Objects.requireNonNull(actionBarSender, "actionBarSender");
        this.reloadTester = Objects.requireNonNull(reloadTester, "reloadTester");
        this.chooser = Objects.requireNonNull(chooser, "chooser");
    }

    @Override
    public void apply(@NotNull GunState state) {
        if (reloadTester.isReloading(state) && state.isMainEquipment()) {
            float progress = (float)state.ticksSinceLastReload() / stats.reloadSpeed();
            actionBarSender.sendActionBar(chooser.choose(state, progress));
            active = true;
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {
        if (!(reloadTester.isReloading(state) && state.isMainEquipment()) && active) {
            actionBarSender.sendActionBar(Component.empty());
            active = false;
        }
    }

    /**
     * Data for an {@link ReloadActionBarEffect}.
     *
     * @param stats                  A path to the guns's {@link GunStats}
     * @param reloadTester           A path to the gun's {@link ReloadTester}
     * @param reloadActionBarChooser A path to the {@link ReloadActionBarEffect}'s {@link ReloadActionBarChooser}
     */
    @DataObject
    public record Data(@NotNull @ChildPath("stats") String stats,
                       @NotNull @ChildPath("action_bar_sender") String actionBarSender,
                       @NotNull @ChildPath("reload_tester") String reloadTester,
                       @NotNull @ChildPath("reload_action_bar_chooser") String reloadActionBarChooser) {
    }
}
