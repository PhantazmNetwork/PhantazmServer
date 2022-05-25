package com.github.phantazmnetwork.mob.target;

import com.github.phantazmnetwork.mob.PhantazmMob;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SelfSelector implements TargetSelector<PhantazmMob<?>> {
    @Override
    public @NotNull Optional<PhantazmMob<?>> selectTarget(@NotNull PhantazmMob<?> mob) {
        return Optional.of(mob);
    }
}
