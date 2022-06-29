package com.github.phantazmnetwork.mob.config;

import com.github.phantazmnetwork.mob.target.TargetSelectorInstance;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@FunctionalInterface
public interface TargetSelectorConfigProcessorSelector {

    <TTarget> @NotNull Optional<TargetSelectorInstance<TTarget>> selectConfigProcessor(@NotNull TargetSelectorInstance<TTarget> selector);

}
