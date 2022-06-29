package com.github.phantazmnetwork.mob;

import com.github.phantazmnetwork.mob.skill.Skill;
import com.github.phantazmnetwork.mob.skill.SkillInstance;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MobStore {

    private final Map<UUID, PhantazmMob> uuidToMob = new HashMap<>();

    private final Map<UUID, Map<Key, Collection<SkillInstance>>> uuidToTriggers = new HashMap<>();

    public void useTrigger(@NotNull Entity entity, @NotNull Key key) {
        UUID uuid = entity.getUuid();
        PhantazmMob mob = uuidToMob.get(uuid);
        if (mob != null) {
            Collection<SkillInstance> triggerInstance = uuidToTriggers.get(uuid).get(key);
            if (triggerInstance != null) {
                for (SkillInstance skillInstance : triggerInstance) {
                    skillInstance.use();
                }
            }
        }
    }

    public void onMobDeath(@NotNull EntityDeathEvent event) {
        UUID uuid = event.getEntity().getUuid();
        uuidToMob.remove(uuid);
        uuidToTriggers.remove(uuid);
    }

    public void registerMob(@NotNull PhantazmMob mob) {
        Objects.requireNonNull(mob, "mob");

        UUID uuid = mob.entity().getUuid();
        if (uuidToMob.containsKey(uuid)) {
            throw new IllegalArgumentException("mob with uuid " + uuid + " already registered");
        }

        uuidToMob.put(uuid, mob);
        uuidToTriggers.put(uuid, createTriggerInstances(mob));
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
