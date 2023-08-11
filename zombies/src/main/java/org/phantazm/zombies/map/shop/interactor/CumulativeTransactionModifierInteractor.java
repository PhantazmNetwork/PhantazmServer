package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.coin.TransactionModifierSource;
import org.phantazm.zombies.map.shop.PlayerInteraction;

@Model("zombies.map.shop.interactor.cumulative_transaction_modifier")
@Cache(false)
public class CumulativeTransactionModifierInteractor implements ShopInteractor {
    private final Data data;
    private final TransactionModifierSource modifierSource;

    @FactoryMethod
    public CumulativeTransactionModifierInteractor(@NotNull Data data,
            @NotNull TransactionModifierSource modifierSource) {
        this.data = data;
        this.modifierSource = modifierSource;
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        modifierSource.addModifier(data.modifierGroup,
                Transaction.modifier(data.displayName, data.modifierAction, data.amount, data.priority));
        return true;
    }

    @DataObject
    public record Data(@NotNull Key modifierGroup,
                       @NotNull Component displayName,
                       @NotNull Transaction.Modifier.Action modifierAction,
                       double amount,
                       int priority) {
        @Default("displayName")
        public static @NotNull ConfigElement defaultDisplayName() {
            return ConfigPrimitive.of("");
        }

        @Default("priority")
        public static @NotNull ConfigElement defaultPriority() {
            return ConfigPrimitive.of(0);
        }
    }
}
