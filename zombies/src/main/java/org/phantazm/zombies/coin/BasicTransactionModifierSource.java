package org.phantazm.zombies.coin;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public class BasicTransactionModifierSource implements TransactionModifierSource {
    private final Map<Key, Collection<Transaction.Modifier>> modifierSources;

    public BasicTransactionModifierSource() {
        this.modifierSources = new HashMap<>();
    }

    @Override
    public @NotNull @UnmodifiableView Collection<Transaction.Modifier> modifiers(@NotNull Key key) {
        Objects.requireNonNull(key, "key");

        Collection<Transaction.Modifier> modifiers = modifierSources.get(key);
        if (modifiers == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableCollection(modifiers);
    }

    @Override
    public void addModifier(@NotNull Key group, Transaction.@NotNull Modifier modifier) {
        Objects.requireNonNull(group, "group");
        Objects.requireNonNull(modifier, "modifier");
        modifierSources.computeIfAbsent(group, m -> new ArrayList<>()).add(modifier);
    }

    @Override
    public void removeModifier(@NotNull Key group, Transaction.@NotNull Modifier modifier) {
        Objects.requireNonNull(group, "group");
        Objects.requireNonNull(modifier, "modifier");
        Collection<Transaction.Modifier> modifiers = modifierSources.get(group);
        if (modifiers != null) {
            modifiers.remove(modifier);
        }
    }
}
