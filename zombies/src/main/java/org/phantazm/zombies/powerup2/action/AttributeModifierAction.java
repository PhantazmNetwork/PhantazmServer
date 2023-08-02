package org.phantazm.zombies.powerup2.action;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.CancellableState;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup2.Powerup;
import org.phantazm.zombies.powerup2.predicate.DeactivationPredicate;
import org.phantazm.zombies.powerup2.predicate.DeactivationPredicateComponent;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

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
        return new Action(data, deactivationPredicate.apply(scene), scene.getZombiesPlayers());
    }

    @DataObject
    public record Data(@NotNull String attribute,
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
        private final Map<? super UUID, ? extends ZombiesPlayer> playerMap;

        private Action(Data data, DeactivationPredicate deactivationPredicate,
                Map<? super UUID, ? extends ZombiesPlayer> playerMap) {
            super(deactivationPredicate);
            this.data = data;
            this.attribute = Objects.requireNonNullElse(Attribute.fromKey(data.attribute), Attributes.NIL);
            this.attributeUID = UUID.randomUUID();
            this.attributeName = attributeUID.toString();
            this.playerMap = playerMap;
        }

        private void applyAttribute(ZombiesPlayer player) {
            player.registerCancellable(CancellableState.named(attributeUID, () -> {
                player.getPlayer().ifPresent(p -> {
                    p.getAttribute(attribute).addModifier(
                            new AttributeModifier(attributeUID, attributeName, data.amount, data.attributeOperation));
                });
            }, () -> {
                player.getPlayer().ifPresent(p -> {
                    p.getAttribute(attribute).removeModifier(attributeUID);
                });
            }), true);
        }

        private void removeAttribute(ZombiesPlayer player) {
            player.removeCancellable(attributeUID);
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
            }
            else {
                applyAttribute(player);
            }
        }

        @Override
        public void deactivate(@NotNull ZombiesPlayer player) {
            if (data.global) {
                for (ZombiesPlayer zombiesPlayer : playerMap.values()) {
                    if (zombiesPlayer.hasQuit()) {
                        continue;
                    }

                    removeAttribute(zombiesPlayer);
                }
            }
            else {
                removeAttribute(player);
            }
        }
    }
}
