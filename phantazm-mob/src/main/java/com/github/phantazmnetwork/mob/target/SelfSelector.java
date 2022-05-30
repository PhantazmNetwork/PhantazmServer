package com.github.phantazmnetwork.mob.target;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.mob.PhantazmMob;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SelfSelector implements TargetSelector<PhantazmMob>, VariantSerializable {

    public final static String SERIAL_NAME = "selfSelector";

    @Override
    public @NotNull Optional<PhantazmMob> selectTarget(@NotNull PhantazmMob mob) {
        return Optional.of(mob);
    }

    @Override
    public @NotNull String getSerialName() {
        return SERIAL_NAME;
    }
}
