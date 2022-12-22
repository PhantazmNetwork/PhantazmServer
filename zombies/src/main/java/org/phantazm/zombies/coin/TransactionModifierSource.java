package org.phantazm.zombies.coin;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

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
                return new AbstractCollection<>() {
                    @Override
                    public Iterator<Transaction.Modifier> iterator() {
                        return new Iterator<>() {
                            private int i = 0; //index of sourcesCopy
                            private Iterator<Transaction.Modifier> iterator = null;

                            @Override
                            public boolean hasNext() {
                                if (i < sourcesCopy.length) {
                                    while ((iterator == null || !iterator.hasNext()) && i < sourcesCopy.length) {
                                        iterator = sourcesCopy[i++].modifiers(key).iterator();
                                    }

                                    return iterator.hasNext();
                                }

                                return false;
                            }

                            @Override
                            public Transaction.Modifier next() {
                                while (iterator == null || !iterator.hasNext()) {
                                    if (i >= sourcesCopy.length) {
                                        throw new NoSuchElementException();
                                    }

                                    iterator = sourcesCopy[i++].modifiers(key).iterator();
                                }

                                return iterator.next();
                            }
                        };
                    }

                    @Override
                    public int size() {
                        int size = 0;
                        for (TransactionModifierSource source : sourcesCopy) {
                            size += source.modifiers(key).size();
                        }

                        return size;
                    }
                };
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
