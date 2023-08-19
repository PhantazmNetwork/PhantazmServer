package org.phantazm.mob;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.skill.Skill;
import org.phantazm.proxima.bindings.minestom.ProximaEntity;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * A representation of a custom mob in Phantazm.
 *
 * @param model  The model for the mob
 * @param entity The actual {@link ProximaEntity} instance of the mob
 */
public record BasicPhantazmMob(@NotNull MobModel model,
    @NotNull ProximaEntity entity,
    @NotNull Map<Key, Collection<Skill>> triggers) implements PhantazmMob {

    /**
     * Creates a PhantazmMob instance
     *
     * @param model  The model for the mob
     * @param entity The actual {@link ProximaEntity} instance of the mob
     */
    public BasicPhantazmMob {
        Objects.requireNonNull(model);
        Objects.requireNonNull(entity);
        Objects.requireNonNull(triggers);
    }

    @Override
    public int hashCode() {
        return entity.getUuid().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof BasicPhantazmMob mob) && mob.entity.getUuid().equals(entity.getUuid());
    }
}
