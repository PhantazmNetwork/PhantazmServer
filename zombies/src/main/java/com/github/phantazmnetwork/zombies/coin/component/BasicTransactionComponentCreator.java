package com.github.phantazmnetwork.zombies.coin.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BasicTransactionComponentCreator implements TransactionComponentCreator {
    @Override
    public @NotNull Component createTransactionComponent(@NotNull List<Component> modifierNames, int change) {
        TextComponent.Builder builder = Component.text().color(NamedTextColor.GOLD);
        if (change >= 0) {
            builder.append(Component.text("+"));
        }
        else {
            builder.append(Component.text("-"));
        }

        builder.append(Component.text(change));
        builder.append(Component.text(" coins"));

        if (!modifierNames.isEmpty()) {
            builder.append(Component.text(" ("));
            for (int i = 0; i < modifierNames.size(); i++) {
                if (i > 0) {
                    builder.append(Component.text(", "));
                }
                builder.append(modifierNames.get(i));
            }
            builder.append(Component.text(")"));
        }

        return builder.build();
    }
}
