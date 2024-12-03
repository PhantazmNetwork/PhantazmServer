package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.selector.SelectorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

@Model("zombies.upgrade.effect.store_target")
@Cache
public class StoreTargetEffect implements UpgradeEffectComponent {
    private final Data data;
    private final SelectorComponent selector;

    @FactoryMethod
    public StoreTargetEffect(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector) {
        this.data = data;
        this.selector = selector;
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return null;
    }

    private static final class Internal implements UpgradeEffect {

        @Override
        public void apply(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer, @NotNull TriggerData triggerData) {

        }

        @Override
        public void clear(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer) {
            UpgradeEffect.super.clear(upgrade, zombiesPlayer);
        }
    }

    @Default("""
        {
          selector={type='zombies.upgrade.selector.event', useTarget=true}
        }
        """)
    @DataObject
    public record Data(@NotNull String tag) {}
}
