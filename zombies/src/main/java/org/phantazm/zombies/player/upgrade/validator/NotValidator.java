package org.phantazm.zombies.player.upgrade.validator;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

@Model("zombies.upgrade.validator.not")
@Cache
public class NotValidator implements ValidatorComponent {
    private final ValidatorComponent validator;

    @FactoryMethod
    public NotValidator(@NotNull @Child("delegate") ValidatorComponent validator) {
        this.validator = validator;
    }

    @Override
    public @NotNull Validator apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(validator.apply(injectionStore, zombiesPlayer));
    }

    private record Internal(Validator delegate) implements Validator {
        @Override
        public boolean test(@NotNull Entity entity, @NotNull PlayerUpgrade playerUpgrade, @NotNull ZombiesPlayer zombiesPlayer, @NotNull TriggerData triggerData) {
            return !delegate.test(entity, playerUpgrade, zombiesPlayer, triggerData);
        }
    }
}
