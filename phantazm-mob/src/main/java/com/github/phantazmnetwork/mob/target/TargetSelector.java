package com.github.phantazmnetwork.mob.target;

import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@FunctionalInterface
public interface TargetSelector<TTarget> {

    @NotNull Optional<TTarget> selectTarget(@NotNull PhantazmMob mob) throws ConfigProcessException;

}
