package com.github.phantazmnetwork.zombies.map.shop.interactor;

import com.github.phantazmnetwork.zombies.coin.ModifierSource;
import com.github.phantazmnetwork.zombies.coin.PlayerCoins;
import com.github.phantazmnetwork.zombies.coin.Transaction;
import com.github.phantazmnetwork.zombies.coin.TransactionResult;
import com.github.phantazmnetwork.zombies.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

@Model("zombies.map.shop.interactor.deduct_coins")
public class DeductCoinsInteractor extends InteractorBase<DeductCoinsInteractor.Data> {
    private final ModifierSource modifierSource;

    @FactoryMethod
    public DeductCoinsInteractor(@NotNull Data data,
            @NotNull @Dependency("zombies.dependency.map_object.modifier_source") ModifierSource modifierSource) {
        super(data);
        this.modifierSource = modifierSource;
    }

    @Override
    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        PlayerCoins coins = interaction.player().getModule().getCoins();
        TransactionResult result =
                coins.runTransaction(new Transaction(modifierSource.modifiers(data.modifierType), -data.cost));
        coins.applyTransaction(result);
    }

    @DataObject
    public record Data(int cost, @NotNull Key modifierType) {
    }
}
