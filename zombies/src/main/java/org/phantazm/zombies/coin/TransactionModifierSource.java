package org.phantazm.zombies.coin;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public interface TransactionModifierSource {
    TransactionModifierSource EMPTY = new TransactionModifierSource() {
        @Override
        public @NotNull Collection<Transaction.Modifier> modifiers(@NotNull Key key) {
            return List.of();
        }

        @Override
        public void addModifier(@NotNull Key group, Transaction.@NotNull Modifier modifier) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeModifier(@NotNull Key group, Transaction.@NotNull Modifier modifier) {
            throw new UnsupportedOperationException();
        }
    };

    @NotNull @UnmodifiableView Collection<Transaction.Modifier> modifiers(@NotNull Key key);

    void addModifier(@NotNull Key group, @NotNull Transaction.Modifier modifier);

    void removeModifier(@NotNull Key group, @NotNull Transaction.Modifier modifier);

    static @NotNull TransactionModifierSource compositeView() {
        return EMPTY;
    }

    static @NotNull TransactionModifierSource compositeView(
            @NotNull TransactionModifierSource transactionModifierSource) {
        Objects.requireNonNull(transactionModifierSource, "transactionModifierSource");

        return new TransactionModifierSource() {
            @Override
            public @NotNull @UnmodifiableView Collection<Transaction.Modifier> modifiers(@NotNull Key key) {
                return transactionModifierSource.modifiers(key);
            }

            @Override
            public void addModifier(@NotNull Key group, Transaction.@NotNull Modifier modifier) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void removeModifier(@NotNull Key group, Transaction.@NotNull Modifier modifier) {
                throw new UnsupportedOperationException();
            }
        };
    }

    static @NotNull TransactionModifierSource compositeView(@NotNull TransactionModifierSource @NotNull ... sources) {
        if (sources.length == 0) {
            return EMPTY;
        }

        if (sources.length == 1) {
            return compositeView(sources[0]);
        }

        TransactionModifierSource[] sourcesCopy = new TransactionModifierSource[sources.length];
        for (int i = 0; i < sources.length; i++) {
            sourcesCopy[i] = Objects.requireNonNull(sources[i], "sources array element");
        }

        return new TransactionModifierSource() {
            @Override
            public @NotNull Collection<Transaction.Modifier> modifiers(@NotNull Key key) {
                int size = 0;
                for (TransactionModifierSource source : sourcesCopy) {
                    size += source.modifiers(key).size();
                }

                List<Transaction.Modifier> modifiers = new ArrayList<>(size);
                for (TransactionModifierSource transactionModifierSource : sourcesCopy) {
                    modifiers.addAll(transactionModifierSource.modifiers(key));
                }

                modifiers.sort(Comparator.comparing(Transaction.Modifier::priority).reversed());
                return Collections.unmodifiableCollection(modifiers);
            }

            @Override
            public void addModifier(@NotNull Key group, Transaction.@NotNull Modifier modifier) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void removeModifier(@NotNull Key group, Transaction.@NotNull Modifier modifier) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
