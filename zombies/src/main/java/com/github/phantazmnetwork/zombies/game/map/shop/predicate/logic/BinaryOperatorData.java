package com.github.phantazmnetwork.zombies.game.map.shop.predicate.logic;

import com.github.phantazmnetwork.commons.Prioritized;
import org.jetbrains.annotations.NotNull;

public interface BinaryOperatorData extends Prioritized {
    @NotNull String firstPath();

    @NotNull String secondPath();
}
