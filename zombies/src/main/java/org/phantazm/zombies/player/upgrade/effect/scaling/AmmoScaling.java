package org.phantazm.zombies.player.upgrade.effect.scaling;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.ScalingFormulae;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.event.trait.GunEvent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

@Model("zombies.upgrade.effect.scaling.ammo")
@Cache
public class AmmoScaling implements ScalingComponent {
    private final Data data;

    @FactoryMethod
    public AmmoScaling(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull Scaling apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data);
    }

    private static final class Internal implements Scaling {
        private final Data data;

        private final boolean largerNumberIsStart;
        private final double largerNumber;
        private final double smallerNumber;

        private Internal(Data data) {
            this.data = data;

            largerNumber = Math.max(data.start, data.end);
            smallerNumber = Math.min(data.start, data.end);
            largerNumberIsStart = data.start > data.end;
        }

        @Override
        public double getMultiplier(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer, @NotNull TriggerData triggerData) {
            if (triggerData.raw() instanceof GunEvent gunEvent) {
                Gun gun = gunEvent.gun();
                int maxAmmo = gun.getLevel().stats().maxAmmo();
                int currentAmmo = gun.getState().ammo();

                double percentage = (double) currentAmmo / maxAmmo;
                return ScalingFormulae.computeMultiplier(data.returnZeroOutsideRange, percentage, largerNumberIsStart, largerNumber,
                    smallerNumber, data.startMultiplier, data.endMultiplier);
            }
            return 1.0;
        }
    }

    @Default("""
        {
          start=1.0,
          end=0.0,
          returnZeroOutsideRange=false
        }
        """
    )
    @DataObject
    public record Data(
        double start,
        double end,
        double startMultiplier,
        double endMultiplier,
        boolean returnZeroOutsideRange
    ){}
}
