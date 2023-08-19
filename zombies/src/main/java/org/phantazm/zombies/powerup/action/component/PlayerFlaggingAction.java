package org.phantazm.zombies.powerup.action.component;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;
import org.phantazm.zombies.powerup.action.PowerupAction;
import org.phantazm.zombies.powerup.action.PowerupActionBase;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicate;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicateComponent;
import org.phantazm.zombies.scene.ZombiesScene;

@Model("zombies.powerup.action.player_flagging")
@Cache(false)
public class PlayerFlaggingAction implements PowerupActionComponent {
    private final Data data;
    private final DeactivationPredicateComponent deactivationPredicate;

    @FactoryMethod
    public PlayerFlaggingAction(@NotNull Data data,
        @NotNull @Child("deactivation_predicate") DeactivationPredicateComponent deactivationPredicate) {
        this.data = data;
        this.deactivationPredicate = deactivationPredicate;
    }

    @Override
    public @NotNull PowerupAction apply(@NotNull ZombiesScene scene) {
        return new Action(data, deactivationPredicate.apply(scene));
    }

    @DataObject
    public record Data(@NotNull Key flag,
        @NotNull @ChildPath("deactivation_predicate") String deactivationPredicate) {
    }

    private static class Action extends PowerupActionBase {
        private final Data data;

        private Action(Data data, DeactivationPredicate deactivationPredicate) {
            super(deactivationPredicate);
            this.data = data;
        }

        @Override
        public void activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer player, long time) {
            super.activate(powerup, player, time);
            player.flags().setFlag(data.flag);
        }

        @Override
        public void deactivate(@NotNull ZombiesPlayer player) {
            player.flags().clearFlag(data.flag);
        }
    }
}
