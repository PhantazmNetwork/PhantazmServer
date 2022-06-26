package com.github.phantazmnetwork.mob.target;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.mob.PhantazmMob;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SelfSelector implements TargetSelector<PhantazmMob>, VariantSerializable {

    public final static Key SERIAL_KEY = Key.key("phantazm", "self_selector");

    @Override
    public @NotNull Optional<PhantazmMob> selectTarget(@NotNull PhantazmMob mob) {
        return Optional.of(mob);
    }

    @Override
    public @NotNull Key getSerialKey() {
        return SERIAL_KEY;
    }
}
