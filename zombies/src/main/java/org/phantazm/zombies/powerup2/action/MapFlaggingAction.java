package org.phantazm.zombies.powerup2.action;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Flaggable;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup2.Powerup;
import org.phantazm.zombies.powerup2.predicate.DeactivationPredicate;
import org.phantazm.zombies.powerup2.predicate.DeactivationPredicateComponent;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.Objects;
import java.util.function.Supplier;

@Model("zombies.powerup.action.map_flagging")
@Cache(false)
public class MapFlaggingAction implements PowerupActionComponent {
    private final Data data;
    private final DeactivationPredicateComponent deactivationPredicate;

    @FactoryMethod
    public MapFlaggingAction(@NotNull Data data,
            @NotNull @Child("deactivation_predicate") DeactivationPredicateComponent deactivationPredicate) {
        this.data = data;
        this.deactivationPredicate = deactivationPredicate;
    }

    @Override
    public @NotNull PowerupAction apply(@NotNull ZombiesScene scene) {
        return new Action(data, deactivationPredicate.apply(scene), scene.getMap().mapObjects().module().flags());
    }

    @DataObject
    public record Data(@NotNull Key flag, @NotNull @ChildPath("deactivation_predicate") String deactivationPredicate) {
    }

    private static class Action extends PowerupActionBase {
        private final Data data;
        private final Flaggable flaggable;

        private Action(Data data, DeactivationPredicate deactivationPredicate, Flaggable flaggable) {
            super(deactivationPredicate);
            this.data = data;
            this.flaggable = flaggable;
        }

        @Override
        public void activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer player, long time) {
            super.activate(powerup, player, time);
            flaggable.setFlag(data.flag);
        }

        @Override
        public void deactivate(@NotNull ZombiesPlayer player) {
            flaggable.clearFlag(data.flag);
        }
    }
}
