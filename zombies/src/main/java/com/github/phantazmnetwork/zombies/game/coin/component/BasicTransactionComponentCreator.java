package com.github.phantazmnetwork.zombies.game.coin.component;

import com.github.phantazmnetwork.zombies.game.coin.Transaction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BasicTransactionComponentCreator implements TransactionComponentCreator {
    @Override
    public @NotNull Component createTransactionComponent(@NotNull List<Transaction.Modifier> modifiers, int change) {
        TextComponent.Builder builder = Component.text().color(NamedTextColor.GOLD);
        if (change >= 0) {
            builder.append(Component.text("+"));
        }
        else {
            builder.append(Component.text("-"));
        }

        builder.append(Component.text(change));
        builder.append(Component.text(" coins"));

        if (!modifiers.isEmpty()) {
            builder.append(Component.text(" ("));
            for (int i = 0; i < modifiers.size(); i++) {
                if (i > 0) {
                    builder.append(Component.text(", "));
                }
                builder.append(modifiers.get(i).getDisplayName());
            }
            builder.append(Component.text(")"));
        }

        return builder.build();
    }
}
