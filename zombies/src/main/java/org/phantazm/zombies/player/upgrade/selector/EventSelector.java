package org.phantazm.zombies.player.upgrade.selector;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.event.trait.EntityEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.Target;
import org.phantazm.zombies.event.trait.EntityTargetEvent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;
import org.phantazm.zombies.player.upgrade.validator.Validator;
import org.phantazm.zombies.player.upgrade.validator.ValidatorComponent;

@Model("zombies.upgrade.selector.event")
@Cache
public class EventSelector implements SelectorComponent {
    private final Data data;
    private final ValidatorComponent validator;

    @FactoryMethod
    public EventSelector(@NotNull Data data, @NotNull @Child("validator") ValidatorComponent validator) {
        this.data = data;
        this.validator = validator;
    }

    @Override
    public @NotNull Selector apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data, validator.apply(injectionStore, zombiesPlayer));
    }

    private static final class Internal implements Selector {
        private final Data data;
        private final Validator validator;

        private Internal(Data data, Validator validator) {
            this.data = data;
            this.validator = validator;
        }

        @Override
        public @NotNull Target select(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            if (data.useTarget && triggerData.raw() instanceof EntityTargetEvent event) {
                return validator.test(event.target()) ? Target.entities(event.target()) : Target.NONE;
            }

            if (triggerData.raw() instanceof EntityEvent entityEvent) {
                return validator.test(entityEvent.getEntity()) ? Target.entities(entityEvent.getEntity()) : Target.NONE;
            }

            return Target.NONE;
        }
    }

    @DataObject
    public record Data(boolean useTarget) {
    }
}
