package com.github.phantazmnetwork.zombies.map.shop.predicate;

import com.github.phantazmnetwork.zombies.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

@Model("zombies.map.shop.predicate.player_flag")
public class PlayerFlagPredicate extends PredicateBase<PlayerFlagPredicate.Data> {

    @FactoryMethod
    public PlayerFlagPredicate(@NotNull Data data) {
        super(data);
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        return interaction.player().flags().hasFlag(data.flag) != data.requireAbsent;
    }

    @DataObject
    public record Data(@NotNull Key flag, boolean requireAbsent) {
    }
}
