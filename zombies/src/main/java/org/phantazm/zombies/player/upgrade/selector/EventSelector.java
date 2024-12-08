package org.phantazm.zombies.player.upgrade.selector;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.Target;
import org.phantazm.zombies.EventUtils;
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

    private record Internal(Data data,
        Validator validator) implements Selector {

        @Override
        public @NotNull Target select(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            if (!(triggerData.raw() instanceof EntityEvent entityEvent)) return Target.NONE;

            Entity target = EventUtils.extractEntity(entityEvent, data.useTarget);
            return validator.test(target, upgrade, zombiesPlayer, triggerData) ? Target.entities(target) : Target.NONE;
        }
    }

    @DataObject
    @Default("""
        {
          validator={type='zombies.upgrade.validator.always'}
        }
        """)
    public record Data(boolean useTarget) {
    }
}
