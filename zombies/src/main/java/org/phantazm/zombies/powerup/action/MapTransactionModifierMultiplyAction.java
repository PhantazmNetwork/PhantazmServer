package org.phantazm.zombies.powerup.action;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.coin.TransactionModifierSource;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicate;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicateComponent;
import org.phantazm.zombies.scene.ZombiesScene;

@Model("zombies.powerup.action.map_transaction_modifier.multiply")
@Cache(false)
public class MapTransactionModifierMultiplyAction implements PowerupActionComponent {
    private final Data data;
    private final DeactivationPredicateComponent deactivationPredicate;

    @FactoryMethod
    public MapTransactionModifierMultiplyAction(@NotNull Data data,
            @NotNull @Child("deactivation_predicate") DeactivationPredicateComponent deactivationPredicate) {
        this.data = data;
        this.deactivationPredicate = deactivationPredicate;
    }


    @Override
    public @NotNull PowerupAction apply(@NotNull ZombiesScene scene) {
        return new Action(data, deactivationPredicate.apply(scene),
                scene.getMap().mapObjects().module().modifierSource());
    }

    @DataObject
    public record Data(@NotNull @ChildPath("deactivation_predicate") String deactivationPredicate,
                       @NotNull Key modifierGroup,
                       @NotNull Component displayName,
                       double factor,
                       int priority) {
    }

    private static class Action extends MapTransactionAction {
        private final Data data;

        private Action(Data data, DeactivationPredicate deactivationPredicate,
                TransactionModifierSource transactionModifierSource) {
            super(deactivationPredicate, transactionModifierSource, data.modifierGroup);
            this.data = data;
        }

        @Override
        protected @NotNull Transaction.Modifier getModifier() {
            return new Transaction.Modifier() {
                @Override
                public @NotNull Component getDisplayName() {
                    return data.displayName;
                }

                @Override
                public int modify(int coins) {
                    return (int)Math.rint(data.factor * coins);
                }

                @Override
                public int getPriority() {
                    return data.priority;
                }
            };
        }
    }
}
