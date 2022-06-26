package com.github.phantazmnetwork.mob.config;

import com.github.phantazmnetwork.mob.target.TargetSelector;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@FunctionalInterface
public interface TargetSelectorConfigProcessorSelector {

    <TTarget> @NotNull Optional<TargetSelector<TTarget>> selectConfigProcessor(@NotNull TargetSelector<TTarget> selector);

}
