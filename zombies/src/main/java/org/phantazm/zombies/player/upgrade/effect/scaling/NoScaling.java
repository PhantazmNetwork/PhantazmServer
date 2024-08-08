package org.phantazm.zombies.player.upgrade.effect.scaling;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

@Model("zombies.upgrade.effect.scaling.none")
@Cache
public class NoScaling implements ScalingComponent {
    private static final Scaling INSTANCE = new Internal();

    @FactoryMethod
    public NoScaling() {
    }

    @Override
    public @NotNull Scaling apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return INSTANCE;
    }

    private static final class Internal implements Scaling {
        @Override
        public double getMultiplier(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            return 1;
        }
    }
}
