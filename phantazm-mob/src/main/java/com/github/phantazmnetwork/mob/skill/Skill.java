package com.github.phantazmnetwork.mob.skill;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.mob.PhantazmMob;
import org.jetbrains.annotations.NotNull;

public interface Skill extends VariantSerializable {

    void use(@NotNull PhantazmMob<?> sender);

}
