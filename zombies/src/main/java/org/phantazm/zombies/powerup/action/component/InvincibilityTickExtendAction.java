package org.phantazm.zombies.powerup.action.component;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;
import org.phantazm.zombies.powerup.action.PowerupAction;
import org.phantazm.zombies.powerup.action.PowerupActionBase;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicate;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicateComponent;
import org.phantazm.zombies.scene2.ZombiesScene;

@Model("zombies.powerup.action.set_invincibility_ticks")
@Cache(false)
public class InvincibilityTickExtendAction implements PowerupActionComponent {
    private final Data data;
    private final DeactivationPredicateComponent deactivationPredicate;

    @FactoryMethod
    public InvincibilityTickExtendAction(@NotNull Data data,
        @NotNull @Child("deactivationPredicate") DeactivationPredicateComponent deactivationPredicate) {
        this.data = data;
        this.deactivationPredicate = deactivationPredicate;
    }

    @Override
    public @NotNull PowerupAction apply(@NotNull ZombiesScene zombiesScene) {
        return new Action(deactivationPredicate.apply(zombiesScene), data.ticks);
    }

    @DataObject
    public record Data(int ticks) {
    }

    private static class Action extends PowerupActionBase {
        private final int ticks;

        private Action(DeactivationPredicate deactivationPredicate, int ticks) {
            super(deactivationPredicate);
            this.ticks = ticks;
        }

        @Override
        public void activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer player, long time) {
            super.activate(powerup, player, time);
            player.getPlayer().ifPresent(actualPlayer -> {
                actualPlayer.setDefaultInvulnerabilityTicks(ticks);
            });
        }

        @Override
        public void deactivate(@NotNull ZombiesPlayer player) {
            player.getPlayer().ifPresent(actualPlayer -> {
                actualPlayer.setDefaultInvulnerabilityTicks(8);
            });
        }
    }
}
