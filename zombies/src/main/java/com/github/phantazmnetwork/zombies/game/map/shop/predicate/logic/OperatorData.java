package com.github.phantazmnetwork.zombies.game.map.shop.predicate.logic;

import com.github.phantazmnetwork.commons.Prioritized;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface OperatorData extends Prioritized {
    @NotNull List<String> paths();
}
