package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.effect.scaling.Scaling;
import org.phantazm.zombies.player.upgrade.effect.scaling.ScalingComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

import java.util.UUID;

@Model("zombies.upgrade.effect.adjust_wave_delay_attribute")
@Cache
public class AdjustWaveDelayAttributeEffect implements UpgradeEffectComponent {
    private final Data data;
    private final ScalingComponent scalingComponent;

    @FactoryMethod
    public AdjustWaveDelayAttributeEffect(@NotNull Data data, @NotNull @Child("scaling") ScalingComponent scalingComponent) {
        this.data = data;
        this.scalingComponent = scalingComponent;
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data, scalingComponent.apply(injectionStore, zombiesPlayer));
    }

    private static final class Internal implements UpgradeEffect {
        private final Data data;
        private final Scaling scaling;
        private final UUID uuid;
        private final String attributeName;

        private Internal(Data data, Scaling scaling) {
            this.data = data;
            this.scaling = scaling;

            this.uuid = UUID.randomUUID();
            this.attributeName = this.uuid.toString();
        }

        @Override
        public void apply(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer, @NotNull TriggerData triggerData) {
            double amount = data.amount * scaling.getMultiplier(upgrade, zombiesPlayer, triggerData);
            AttributeInstance attr = zombiesPlayer.getScene().map().roundHandler().waveDelayAttribute();

            attr.removeModifier(uuid);

            if (amount != 0) {
                attr.addModifier(new AttributeModifier(uuid, attributeName, amount, data.operation));
            }
        }

        @Override
        public void clear(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer) {
            zombiesPlayer.getScene().map().roundHandler().waveDelayAttribute().removeModifier(uuid);
        }
    }

    @Default("""
        {
          scaling={type='zombies.upgrade.effect.scaling.none'}
        }
        """)
    @DataObject
    public record Data(double amount,
        @NotNull AttributeOperation operation) {}
}
