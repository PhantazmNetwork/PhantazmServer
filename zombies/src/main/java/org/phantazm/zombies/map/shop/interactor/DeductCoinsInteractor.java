package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.coin.PlayerCoins;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.coin.TransactionResult;
import org.phantazm.zombies.map.shop.PlayerInteraction;

@Model("zombies.map.shop.interactor.deduct_coins")
public class DeductCoinsInteractor extends InteractorBase<DeductCoinsInteractor.Data> {
    @FactoryMethod
    public DeductCoinsInteractor(@NotNull Data data) {
        super(data);
    }

    @Override
    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        PlayerCoins coins = interaction.player().getModule().getCoins();
        TransactionResult result = coins.runTransaction(new Transaction(
                interaction.player().getModule().compositeTransactionModifiers().modifiers(data.modifierType),
                -data.cost));
        coins.applyTransaction(result);
    }

    @DataObject
    public record Data(int cost, @NotNull Key modifierType) {
    }
}
