package org.phantazm.zombies.coin;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public class BasicTransactionModifierSource implements TransactionModifierSource {
    private final Map<String, Collection<Transaction.Modifier>> modifierSources;

    public BasicTransactionModifierSource() {
        this.modifierSources = new HashMap<>();
    }

    @Override
    public @NotNull @UnmodifiableView Collection<Transaction.Modifier> modifiers(@NotNull Key key) {
        Objects.requireNonNull(key, "key");

        String namespace = key.namespace();
        String value = key.value();

        String[] parts = value.split("\\.");

        List<Transaction.Modifier> mergedModifiers = new ArrayList<>(3);
        StringBuilder current = new StringBuilder(namespace + ":");
        for (String part : parts) {
            Collection<Transaction.Modifier> modifiers = modifierSources.get(current.append(part).toString());
            if (modifiers != null) {
                mergedModifiers.addAll(modifiers);
            }

            current.append('.');
        }

        mergedModifiers.sort(Comparator.comparing(Transaction.Modifier::getPriority).reversed());
        return Collections.unmodifiableCollection(mergedModifiers);
    }

    @Override
    public void addModifier(@NotNull Key group, Transaction.@NotNull Modifier modifier) {
        Objects.requireNonNull(group, "group");
        Objects.requireNonNull(modifier, "modifier");

        modifierSources.computeIfAbsent(group.asString(), (ignored) -> new LinkedHashSet<>(4)).add(modifier);
    }

    @Override
    public void removeModifier(@NotNull Key group, Transaction.@NotNull Modifier modifier) {
        Objects.requireNonNull(group, "group");
        Objects.requireNonNull(modifier, "modifier");

        Collection<Transaction.Modifier> modifiers = modifierSources.get(group.asString());
        if (modifiers != null) {
            modifiers.remove(modifier);
        }
    }
}
