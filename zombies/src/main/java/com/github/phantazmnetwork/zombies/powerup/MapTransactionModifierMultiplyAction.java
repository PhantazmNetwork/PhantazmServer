package com.github.phantazmnetwork.zombies.powerup;

import com.github.phantazmnetwork.zombies.coin.TransactionModifierSource;
import com.github.phantazmnetwork.zombies.coin.Transaction;
import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

@Model("zombies.powerup.action.map_transaction_modifier.multiply")
public class MapTransactionModifierMultiplyAction implements Supplier<PowerupAction> {
    private final Data data;
    private final TransactionModifierSource transactionModifierSource;
    private final Supplier<DeactivationPredicate> deactivationPredicate;

    @FactoryMethod
    public MapTransactionModifierMultiplyAction(@NotNull Data data,
            @NotNull @DataName("deactivation_predicate") Supplier<DeactivationPredicate> deactivationPredicate,
            @Dependency("zombies.dependency.map_object.modifier_source")
            TransactionModifierSource transactionModifierSource) {
        this.data = Objects.requireNonNull(data, "data");
        this.transactionModifierSource = transactionModifierSource;
        this.deactivationPredicate = Objects.requireNonNull(deactivationPredicate, "deactivationPredicate");
    }

    @Override
    public PowerupAction get() {
        return new Action(data, deactivationPredicate.get(), transactionModifierSource);
    }

    @DataObject
    public record Data(@NotNull @DataPath("deactivation_predicate") String deactivationPredicate,
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