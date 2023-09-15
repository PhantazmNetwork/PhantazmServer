package org.phantazm.zombies.powerup.action.component;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.event.GunLoseAmmoEvent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;
import org.phantazm.zombies.powerup.action.PowerupAction;
import org.phantazm.zombies.powerup.action.PowerupActionBase;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicate;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicateComponent;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.UUID;

@Model("zombies.powerup.action.prevent_ammo_drain")
@Cache(false)
public class PreventAmmoDrainAction implements PowerupActionComponent {
    private final DeactivationPredicateComponent deactivationPredicate;

    @FactoryMethod
    public PreventAmmoDrainAction(
        @NotNull @Child("deactivation_predicate") DeactivationPredicateComponent deactivationPredicate) {
        this.deactivationPredicate = deactivationPredicate;
    }

    @Override
    public @NotNull PowerupAction apply(@NotNull ZombiesScene scene) {
        return new Action(deactivationPredicate.apply(scene), scene.sceneNode(), scene.instance().getUniqueId());
    }

    private static class Action extends PowerupActionBase {
        private final EventListener<GunLoseAmmoEvent> listener;
        private final EventNode<Event> eventNode;
        private final UUID instanceUUID;

        private Action(DeactivationPredicate deactivationPredicate, EventNode<Event> eventNode, UUID instanceUUID) {
            super(deactivationPredicate);
            this.listener = EventListener.of(GunLoseAmmoEvent.class, this::gunLoseAmmo);
            this.eventNode = eventNode;
            this.instanceUUID = instanceUUID;
        }

        private void gunLoseAmmo(GunLoseAmmoEvent event) {
            if (!event.getInstance().getUniqueId().equals(instanceUUID)) {
                return;
            }

            event.setAmmoLost(0);
        }

        @Override
        public void activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer player, long time) {
            super.activate(powerup, player, time);
            eventNode.addListener(this.listener);
        }

        @Override
        public void deactivate(@NotNull ZombiesPlayer player) {
            eventNode.removeListener(listener);
        }
    }

    @DataObject
    public record Data(@NotNull @ChildPath("deactivation_predicate") String deactivationPredicate) {

    }
}
