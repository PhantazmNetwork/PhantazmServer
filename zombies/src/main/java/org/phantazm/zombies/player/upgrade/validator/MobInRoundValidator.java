package org.phantazm.zombies.player.upgrade.validator;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.player.ZombiesPlayer;

@Model("zombies.upgrade.validator.mob_in_round")
@Cache
public class MobInRoundValidator implements ValidatorComponent {
    @FactoryMethod
    public MobInRoundValidator() {

    }

    @Override
    public @NotNull Validator apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(zombiesPlayer);
    }

    private record Internal(ZombiesPlayer zombiesPlayer) implements Validator {
        @Override
        public boolean test(Entity entity) {
            return entity instanceof Mob mob && zombiesPlayer.getScene().map().roundHandler().currentRound()
                .map(round -> round.hasMob(mob.getUuid()))
                .orElse(false);
        }
    }
}
