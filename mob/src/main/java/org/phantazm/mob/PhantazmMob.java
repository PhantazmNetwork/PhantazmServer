package org.phantazm.mob;

import net.kyori.adventure.key.Key;
import org.phantazm.mob.skill.Skill;
import org.phantazm.proxima.bindings.minestom.ProximaEntity;

import java.util.Collection;
import java.util.Map;

public interface PhantazmMob {
    MobModel model();

    ProximaEntity entity();

    Map<Key, Collection<Skill>> triggers();
}
