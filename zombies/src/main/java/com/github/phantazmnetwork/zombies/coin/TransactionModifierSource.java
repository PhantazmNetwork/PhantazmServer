package com.github.phantazmnetwork.zombies.coin;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public interface TransactionModifierSource {
    TransactionModifierSource EMPTY = new TransactionModifierSource() {
        @Override
        public @NotNull Collection<Transaction.Modifier> modifiers(@NotNull Key key) {
            return Collections.emptyList();
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

    static @NotNull TransactionModifierSource compositeView(@NotNull TransactionModifierSource @NotNull ... sources) {
        if (sources.length == 0) {
            return EMPTY;
        }

        if (sources.length == 1) {
            return Objects.requireNonNull(sources[0], "sources array element");
        }

        TransactionModifierSource[] sourcesCopy = new TransactionModifierSource[sources.length];
        for (int i = 0; i < sources.length; i++) {
            sourcesCopy[i] = Objects.requireNonNull(sources[i], "sources array element");
        }

        return new TransactionModifierSource() {
            @SuppressWarnings("unchecked")
            @Override
            public @NotNull Collection<Transaction.Modifier> modifiers(@NotNull Key key) {
                Collection<?>[] array = new Collection[sourcesCopy.length];

                int totalLength = 0;
                for (int i = 0; i < sourcesCopy.length; i++) {
                    Collection<Transaction.Modifier> collection = sourcesCopy[i].modifiers(key);
                    totalLength += collection.size();

                    array[i] = collection;
                }

                Collection<Transaction.Modifier> combined = new ArrayList<>(totalLength);
                for (Collection<?> collection : array) {
                    combined.addAll((Collection<? extends Transaction.Modifier>)collection);
                }

                return Collections.unmodifiableCollection(combined);
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
