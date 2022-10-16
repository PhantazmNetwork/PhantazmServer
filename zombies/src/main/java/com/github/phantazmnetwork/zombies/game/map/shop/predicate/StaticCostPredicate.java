package com.github.phantazmnetwork.zombies.game.map.shop.predicate;

import com.github.phantazmnetwork.zombies.game.coin.ModifierSource;
import com.github.phantazmnetwork.zombies.game.coin.PlayerCoins;
import com.github.phantazmnetwork.zombies.game.coin.Transaction;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("zombies.map.shop.predicate.static_cost")
public class StaticCostPredicate extends PredicateBase<StaticCostPredicate.Data> {
    private final ModifierSource modifierSource;

    @FactoryMethod
    public StaticCostPredicate(@NotNull Data data,
            @NotNull @Dependency("zombies.dependency.map_object.modifier_source") ModifierSource modifierSource) {
        super(data);
        this.modifierSource = Objects.requireNonNull(modifierSource, "modifierSource");
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        PlayerCoins coins = interaction.player().getModule().getCoins();
        return coins.runTransaction(new Transaction(modifierSource.modifiers(data.modifierType), -data.cost))
                .isAffordable(coins);
    }

    @DataObject
    public record Data(int cost, @NotNull Key modifierType) {
    }
}
