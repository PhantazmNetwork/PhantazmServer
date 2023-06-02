package org.phantazm.zombies.powerup.action;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicate;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

@Model("zombies.powerup.action.attribute_modifier")
@Cache(false)
public class AttributeModifierAction implements Supplier<PowerupAction> {
    private final Data data;
    private final Supplier<DeactivationPredicate> deactivationPredicate;
    private final Map<? super UUID, ? extends ZombiesPlayer> playerMap;

    @FactoryMethod
    public AttributeModifierAction(@NotNull Data data, @NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap,
            @NotNull @Child("predicate") Supplier<DeactivationPredicate> deactivationPredicate) {
        this.data = data;
        this.playerMap = playerMap;
        this.deactivationPredicate = deactivationPredicate;
    }

    @Override
    public PowerupAction get() {
        return new Action(data, deactivationPredicate.get(), playerMap);
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
            player.getPlayer().ifPresent(p -> {
                p.getAttribute(attribute).addModifier(
                        new AttributeModifier(attributeUID, attributeName, data.amount, data.attributeOperation));
            });
        }

        private void removeAttribute(ZombiesPlayer player) {
            player.getPlayer().ifPresent(p -> {
                p.getAttribute(attribute).removeModifier(attributeUID);
            });
        }

        @Override
        public void activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer player, long time) {
            super.activate(powerup, player, time);

            if (data.global) {
                for (ZombiesPlayer zombiesPlayer : playerMap.values()) {
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
                    removeAttribute(zombiesPlayer);
                }
            }
            else {
                removeAttribute(player);
            }
        }
    }
}
