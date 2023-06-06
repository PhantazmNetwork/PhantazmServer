package org.phantazm.zombies.powerup.action;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.event.GunLoseAmmoEvent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicate;

import java.util.function.Supplier;

@Model("zombies.powerup.action.prevent_ammo_drain")
public class PreventAmmoDrainAction implements Supplier<PowerupAction> {
    private final Supplier<DeactivationPredicate> deactivationPredicate;
    private final EventNode<Event> rootNode;

    @FactoryMethod
    public PreventAmmoDrainAction(
            @NotNull @Child("deactivation_predicate") Supplier<DeactivationPredicate> deactivationPredicate,
            @NotNull EventNode<Event> rootNode) {
        this.deactivationPredicate = deactivationPredicate;
        this.rootNode = rootNode;
    }

    @Override
    public PowerupAction get() {
        return new Action(deactivationPredicate.get(), rootNode);
    }

    private static class Action extends PowerupActionBase {
        private final EventListener<GunLoseAmmoEvent> listener;
        private final EventNode<Event> rootNode;

        private Action(DeactivationPredicate deactivationPredicate, EventNode<Event> rootNode) {
            super(deactivationPredicate);
            this.listener = EventListener.of(GunLoseAmmoEvent.class, this::gunLoseAmmo);
            this.rootNode = rootNode;
        }

        private void gunLoseAmmo(GunLoseAmmoEvent event) {
            event.setAmmoLost(0);
        }

        @Override
        public void activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer player, long time) {
            super.activate(powerup, player, time);
            rootNode.addListener(this.listener);
        }

        @Override
        public void deactivate(@NotNull ZombiesPlayer player) {
            rootNode.removeListener(listener);
        }
    }

    @DataObject
    public record Data(@NotNull @ChildPath("deactivation_predicate") String deactivationPredicate) {

    }
}
