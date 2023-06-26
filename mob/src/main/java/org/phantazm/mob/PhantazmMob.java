package org.phantazm.mob;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.mob.skill.Skill;
import org.phantazm.proxima.bindings.minestom.ProximaEntity;

import java.util.Collection;
import java.util.Map;

public interface PhantazmMob {
    @NotNull MobModel model();

    @NotNull ProximaEntity entity();

    @NotNull @Unmodifiable Map<Key, Collection<Skill>> triggers();
}
