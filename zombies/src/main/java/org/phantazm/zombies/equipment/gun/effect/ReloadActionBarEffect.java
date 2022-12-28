package org.phantazm.zombies.equipment.gun.effect;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.GunStats;
import org.phantazm.zombies.equipment.gun.audience.AudienceProvider;
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
    private final AudienceProvider audienceProvider;
    private final ReloadTester reloadTester;
    private final ReloadActionBarChooser chooser;
    private boolean active = false;

    /**
     * Creates a {@link ReloadActionBarEffect}.
     *
     * @param stats            The gun's {@link GunStats}
     * @param audienceProvider A {@link AudienceProvider} to provide an {@link Audience} to send action bars to
     * @param reloadTester     The gun's {@link ReloadTester}
     * @param chooser          The {@link ReloadActionBarChooser} to choose an action bar to send to the {@link Audience}
     */
    @FactoryMethod
    public ReloadActionBarEffect(@NotNull Data data, @NotNull @Child("stats") GunStats stats,
            @NotNull @Child("audience_provider") AudienceProvider audienceProvider,
            @NotNull @Child("reload_tester") ReloadTester reloadTester,
            @NotNull @Child("reload_action_bar_chooser") ReloadActionBarChooser chooser) {
        this.stats = Objects.requireNonNull(stats, "stats");
        this.audienceProvider = Objects.requireNonNull(audienceProvider, "audienceProvider");
        this.reloadTester = Objects.requireNonNull(reloadTester, "reloadTester");
        this.chooser = Objects.requireNonNull(chooser, "chooser");
    }

    @Override
    public void apply(@NotNull GunState state) {
        if (reloadTester.isReloading(state) && state.isMainEquipment()) {
            float progress = (float)state.ticksSinceLastReload() / stats.reloadSpeed();
            audienceProvider.provideAudience()
                    .ifPresent(audience -> audience.sendActionBar(chooser.choose(state, progress)));
            active = true;
        }
        else if (active) {
            audienceProvider.provideAudience().ifPresent(audience -> audience.sendActionBar(Component.empty()));
            active = false;
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    /**
     * Data for an {@link ReloadActionBarEffect}.
     *
     * @param statsPath                  A path to the guns's {@link GunStats}
     * @param audienceProviderPath       A path to the {@link ReloadActionBarEffect}'s {@link AudienceProvider}
     * @param reloadTesterPath           A path to the gun's {@link ReloadTester}
     * @param reloadActionBarChooserPath A path to the {@link ReloadActionBarEffect}'s {@link ReloadActionBarChooser}
     */
    @DataObject
    public record Data(@NotNull @DataPath("stats") String statsPath,
                       @NotNull @DataPath("audience_provider") String audienceProviderPath,
                       @NotNull @DataPath("reload_tester") String reloadTesterPath,
                       @NotNull @DataPath("reload_action_bar_chooser") String reloadActionBarChooserPath) {

        /**
         * Creates a {@link Data}.
         *
         * @param statsPath                  A path to the guns's {@link GunStats}
         * @param audienceProviderPath       A path to the {@link ReloadActionBarEffect}'s {@link AudienceProvider}
         * @param reloadTesterPath           A path to the gun's {@link ReloadTester}
         * @param reloadActionBarChooserPath A path to the {@link ReloadActionBarEffect}'s {@link ReloadActionBarChooser}
         */
        public Data {
            Objects.requireNonNull(statsPath, "statsPath");
            Objects.requireNonNull(audienceProviderPath, "audienceProviderPath");
            Objects.requireNonNull(reloadTesterPath, "reloadTesterPath");
            Objects.requireNonNull(reloadActionBarChooserPath, "reloadActionBarChooserPath");
        }

    }

}
