package org.phantazm.zombies.player.upgrade.effect.scaling;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

@Model("zombies.upgrade.effect.scaling.constant")
@Cache
public class ConstantScaling implements ScalingComponent {
    private final Data data;

    @FactoryMethod
    public ConstantScaling(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull Scaling apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data);
    }

    private static final class Internal implements Scaling {
        private final Data data;

        private Internal(Data data) {
            this.data = data;
        }

        @Override
        public double getMultiplier(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            return data.value;
        }
    }

    @DataObject
    public record Data(double value) {
    }
}
