package com.github.phantazmnetwork.mob.target;

import com.github.phantazmnetwork.mob.PhantazmMob;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@FunctionalInterface
public interface TargetSelector<TReturn> {

    @NotNull Optional<TReturn> selectTarget(@NotNull PhantazmMob mob);

}
