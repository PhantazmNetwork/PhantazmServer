package org.phantazm.zombies.powerup.action.component;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.state.CancellableState;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.Stages;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;
import org.phantazm.zombies.powerup.action.PowerupAction;
import org.phantazm.zombies.powerup.action.PowerupActionBase;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicate;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicateComponent;
import org.phantazm.zombies.scene2.ZombiesScene;
import org.phantazm.core.player.PlayerView;

import java.util.*;

@Model("zombies.powerup.action.attribute_modifier")
@Cache(false)
public class AttributeModifierAction implements PowerupActionComponent {
    private final Data data;
    private final DeactivationPredicateComponent deactivationPredicate;

    @FactoryMethod
    public AttributeModifierAction(@NotNull Data data,
        @NotNull @Child("predicate") DeactivationPredicateComponent deactivationPredicate) {
        this.data = data;
        this.deactivationPredicate = deactivationPredicate;
    }

    @Override
    public @NotNull PowerupAction apply(@NotNull ZombiesScene scene) {
        return new Action(data, deactivationPredicate.apply(scene), scene.managedPlayers());
    }

    @DataObject
    public record Data(
        @NotNull String attribute,
        double amount,
        @NotNull AttributeOperation attributeOperation,
        boolean global,
        @NotNull @ChildPath("predicate") String deactivationPredicate) {

    }

    private static class Action extends PowerupActionBase {
        private final Data data;
        private final Attribute attribute;
        private final UUID attributeUID;
        private final String attributeName;
        private final Map<PlayerView, ZombiesPlayer> playerMap;

        private final List<CancellableState<Entity>> states;

        private Action(Data data, DeactivationPredicate deactivationPredicate,
            Map<PlayerView, ZombiesPlayer> playerMap) {
            super(deactivationPredicate);
            this.data = data;
            this.attribute = Objects.requireNonNullElse(Attribute.fromKey(data.attribute), Attributes.NIL);
            this.attributeUID = UUID.randomUUID();
            this.attributeName = attributeUID.toString();
            this.playerMap = playerMap;

            this.states = new ArrayList<>();
        }

        private void applyAttribute(ZombiesPlayer zombiesPlayer) {
            zombiesPlayer.getPlayer().ifPresent(actualPlayer -> {
                CancellableState<Entity> state = CancellableState.state(actualPlayer, entity -> {
                    ((Player) entity).getAttribute(attribute).addModifier(
                        new AttributeModifier(attributeUID, attributeName, data.amount, data.attributeOperation));
                }, entity -> {
                    ((Player) entity).getAttribute(attribute).removeModifier(attributeUID);
                });

                actualPlayer.stateHolder().registerState(Stages.ZOMBIES_GAME, state);
                states.add(state);
            });
        }

        @Override
        public void activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer player, long time) {
            super.activate(powerup, player, time);

            if (data.global) {
                for (ZombiesPlayer zombiesPlayer : playerMap.values()) {
                    if (zombiesPlayer.hasQuit()) {
                        continue;
                    }

                    applyAttribute(zombiesPlayer);
                }
            } else {
                applyAttribute(player);
            }
        }

        @Override
        public void deactivate(@NotNull ZombiesPlayer player) {
            for (CancellableState<Entity> state : states) {
                state.self().stateHolder().removeState(Stages.ZOMBIES_GAME, state);
            }

            states.clear();
        }
    }
}
