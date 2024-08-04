package org.phantazm.zombies.player.upgrade.selector;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.Target;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;
import org.phantazm.zombies.player.upgrade.validator.Validator;
import org.phantazm.zombies.player.upgrade.validator.ValidatorComponent;

@Model("zombies.upgrade.selector.self")
@Cache
public class SelfSelector implements SelectorComponent {
    private final ValidatorComponent validator;

    @FactoryMethod
    public SelfSelector(@NotNull @Child("validator") ValidatorComponent validator) {
        this.validator = validator;
    }

    @Override
    public @NotNull Selector apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(validator.apply(injectionStore, zombiesPlayer));
    }

    private record Internal(Validator validator) implements Selector {
        @Override
        public @NotNull Target select(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            Player player = zombiesPlayer.getPlayer().orElse(null);
            if (player == null) {
                return Target.NONE;
            }

            return validator.test(player, upgrade, zombiesPlayer, triggerData) ? Target.entities(player) : Target.NONE;
        }
    }

    @DataObject
    @Default("""
        {
          validator={type='zombies.upgrade.validator.always'}
        }
        """)
    public record Data() {
    }
}
