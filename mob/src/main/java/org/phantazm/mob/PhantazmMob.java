package org.phantazm.mob;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.skill.Skill;
import org.phantazm.neuron.bindings.minestom.entity.NeuralEntity;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * A representation of a custom mob in Phantazm.
 *
 * @param model  The model for the mob
 * @param entity The actual {@link NeuralEntity} instance of the mob
 */
public record PhantazmMob(@NotNull MobModel model,
                          @NotNull NeuralEntity entity,
                          @NotNull Map<Key, Collection<Skill>> triggers) {

    /**
     * Creates a PhantazmMob instance
     *
     * @param model  The model for the mob
     * @param entity The actual {@link NeuralEntity} instance of the mob
     */
    public PhantazmMob {
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(triggers, "triggers");
    }

    @Override
    public int hashCode() {
        return entity.getUuid().hashCode();
    }
}
