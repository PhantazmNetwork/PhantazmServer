package com.github.phantazmnetwork.mob;

import com.github.phantazmnetwork.mob.skill.Skill;
import com.github.phantazmnetwork.mob.skill.SkillInstance;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A store of {@link PhantazmMob}s. Mobs should be registered to the store and events
 * pertaining to mobs should be handled by the store.
 */
public class MobStore {

    private final Map<UUID, PhantazmMob> uuidToMob = new HashMap<>();

    private final Map<UUID, Map<Key, Collection<SkillInstance>>> uuidToTriggers = new HashMap<>();

    /**
     * Attempts to activate triggers for an {@link Entity}.
     *
     * @param entity The {@link Entity} to activate triggers for
     * @param key    The {@link Key} of the {@link net.minestom.server.event.Event} that triggered the activation
     */
    public void useTrigger(@NotNull Entity entity, @NotNull Key key) {
        Map<Key, Collection<SkillInstance>> triggers = uuidToTriggers.get(entity.getUuid());
        if (triggers != null) {
            Collection<SkillInstance> triggerInstance = triggers.get(key);
            if (triggerInstance != null) {
                for (SkillInstance skillInstance : triggerInstance) {
                    skillInstance.use();
                }
            }
        }
    }

    /**
     * Called when an {@link Entity} dies.
     *
     * @param event The {@link EntityDeathEvent} that occurred
     */
    public void onMobDeath(@NotNull EntityDeathEvent event) {
        UUID uuid = event.getEntity().getUuid();
        uuidToMob.remove(uuid);
        uuidToTriggers.remove(uuid);
    }

    /**
     * Registers a {@link PhantazmMob} to the store.
     *
     * @param mob The {@link PhantazmMob} to register
     */
    public void registerMob(@NotNull PhantazmMob mob) {
        Objects.requireNonNull(mob, "mob");

        UUID uuid = mob.entity().getUuid();
        if (uuidToMob.containsKey(uuid)) {
            throw new IllegalArgumentException("Mob with uuid " + uuid + " already registered");
        }

        uuidToMob.put(uuid, mob);
        uuidToTriggers.put(uuid, createTriggerInstances(mob));
    }

    /**
     * Gets the {@link PhantazmMob} associated with a {@link UUID}.
     *
     * @param uuid The {@link UUID} of the {@link PhantazmMob} to get
     * @return The {@link PhantazmMob} associated with the {@link UUID} or null if none exists
     */
    public PhantazmMob getMob(@NotNull UUID uuid) {
        return uuidToMob.get(Objects.requireNonNull(uuid, "uuid"));
    }

    /**
     * Checks if the mob store contains a {@link PhantazmMob} associated with a {@link UUID}.
     *
     * @param uuid The {@link UUID} to check
     * @return Whether a {@link PhantazmMob} is associated with the {@link UUID} in the store
     */
    public boolean hasMob(@NotNull UUID uuid) {
        return uuidToMob.containsKey(Objects.requireNonNull(uuid, "uuid"));
    }

    private @NotNull Map<Key, Collection<SkillInstance>> createTriggerInstances(@NotNull PhantazmMob mob) {
        Map<Key, Collection<SkillInstance>> triggerInstances = new HashMap<>(mob.model().getTriggers().size());
        for (Map.Entry<Key, Collection<Skill>> entry : mob.model().getTriggers().entrySet()) {
            Collection<SkillInstance> skillInstances = new ArrayList<>(entry.getValue().size());
            for (Skill skill : entry.getValue()) {
                skillInstances.add(skill.createSkill(mob));
            }

            triggerInstances.put(entry.getKey(), skillInstances);
        }

        return triggerInstances;
    }

}
