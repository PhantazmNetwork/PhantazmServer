package org.phantazm.zombies.powerup.action.component;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.coin.TransactionModifierSource;
import org.phantazm.zombies.powerup.action.MapTransactionAction;
import org.phantazm.zombies.powerup.action.PowerupAction;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicate;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicateComponent;
import org.phantazm.zombies.scene2.ZombiesScene;

@Model("zombies.powerup.action.map_transaction_modifier")
@Cache(false)
public class MapTransactionModifierAction implements PowerupActionComponent {
    private final Data data;
    private final DeactivationPredicateComponent deactivationPredicate;

    @FactoryMethod
    public MapTransactionModifierAction(@NotNull Data data,
        @NotNull @Child("deactivation_predicate") DeactivationPredicateComponent deactivationPredicate) {
        this.data = data;
        this.deactivationPredicate = deactivationPredicate;
    }

    @Override
    public @NotNull PowerupAction apply(@NotNull ZombiesScene scene) {
        return new Action(data, deactivationPredicate.apply(scene),
            scene.map().mapObjects().module().modifierSource());
    }

    @DataObject
    public record Data(
        @NotNull @ChildPath("deactivation_predicate") String deactivationPredicate,
        @NotNull Key modifierGroup,
        @NotNull Component displayName,
        @NotNull Transaction.Modifier.Action modifierAction,
        double amount,
        int priority) {
        @Default("priority")
        public static @NotNull ConfigElement defaultPriority() {
            return ConfigPrimitive.of(0);
        }
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
            return Transaction.modifier(data.displayName, data.modifierAction, data.amount, data.priority);
        }
    }
}
