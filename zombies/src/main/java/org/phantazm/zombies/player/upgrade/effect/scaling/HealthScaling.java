package org.phantazm.zombies.player.upgrade.effect.scaling;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.selector.Selector;
import org.phantazm.zombies.player.upgrade.selector.SelectorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

import java.util.Optional;

@Model("zombies.upgrade.effect.scaling.health")
@Cache
public class HealthScaling implements ScalingComponent {
    private final Data data;
    private final SelectorComponent selectorComponent;

    @FactoryMethod
    public HealthScaling(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selectorComponent) {
        this.data = data;
        this.selectorComponent = selectorComponent;
    }

    @Override
    public @NotNull Scaling apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data, selectorComponent.apply(injectionStore, zombiesPlayer));
    }

    private static final class Internal implements Scaling {
        private final Data data;
        private final Selector selector;
        private final boolean largerNumberIsStart;
        private final double largerNumber;
        private final double smallerNumber;

        private Internal(Data data, Selector selector) {
            this.data = data;
            this.selector = selector;

            largerNumber = Math.max(data.start, data.end);
            smallerNumber = Math.min(data.start, data.end);
            largerNumberIsStart = data.start > data.end;
        }

        public double getMultiplier(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {

            Optional<LivingEntity> possibleTarget = selector.select(upgrade, zombiesPlayer, triggerData).forType(LivingEntity.class);

            if(possibleTarget.isEmpty()) {
                return 1.0;
            }

            LivingEntity target = possibleTarget.get();
            double percentage = (double) target.getHealth() / target.getMaxHealth();

            if(data.returnZeroOutsideRange) {
                if(percentage > largerNumber || percentage < smallerNumber) {
                    return 0.0;
                }
            } else {
                if(largerNumberIsStart) {
                    if(percentage > largerNumber) {
                        return data.minMultiplier;
                    }
                    if(percentage < smallerNumber) {
                        return data.maxMultiplier;
                    }
                } else {
                    if(percentage > largerNumber) {
                        return data.maxMultiplier;
                    }
                    if(percentage < smallerNumber) {
                        return data.minMultiplier;
                    }
                }
            }

            double range = largerNumber - smallerNumber;
            double minMult = Math.min(data.maxMultiplier, data.minMultiplier);
            double maxMult = Math.max(data.maxMultiplier, data.minMultiplier);
            return largerNumberIsStart ? ((largerNumber - percentage) / range) * (maxMult - minMult) + minMult :
                ((range - (largerNumber - percentage)) / range) * (maxMult - minMult) + minMult;
        }
    }

    @Default("""
        {
          selector={type='zombies.upgrade.selector.self'}
          start=1.0,
          end=0.0,
          maxMultiplier=0.0,
          minMultiplier=0.0,
          returnZeroOutsideRange=false
        }
        """
    )
    @DataObject
    public record Data(
        double start,
        double end,
        double maxMultiplier,
        double minMultiplier,
        boolean returnZeroOutsideRange
    ) {}
}
