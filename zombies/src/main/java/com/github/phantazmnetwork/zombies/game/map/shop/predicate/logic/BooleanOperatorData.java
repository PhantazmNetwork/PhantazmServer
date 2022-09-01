package com.github.phantazmnetwork.zombies.game.map.shop.predicate.logic;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@FunctionalInterface
public interface BooleanOperatorData {
    @NotNull List<String> paths();
}