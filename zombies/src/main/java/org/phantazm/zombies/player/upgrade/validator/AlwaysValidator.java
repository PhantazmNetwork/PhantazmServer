package org.phantazm.zombies.player.upgrade.validator;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

@Model("zombies.upgrade.validator.always")
@Cache
public class AlwaysValidator implements ValidatorComponent {
    private static final Validator INSTANCE = new Internal();

    @FactoryMethod
    public AlwaysValidator() {
    }

    @Override
    public @NotNull Validator apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return INSTANCE;
    }

    private static final class Internal implements Validator {
        @Override
        public boolean test(@NotNull Entity candidate, @NotNull PlayerUpgrade playerUpgrade,
            @NotNull ZombiesPlayer zombiesPlayer, @NotNull TriggerData triggerData) {
            return true;
        }
    }
}
