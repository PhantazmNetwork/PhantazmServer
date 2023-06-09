package org.phantazm.mob;

import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Namespaces;
import org.phantazm.commons.Tickable;
import org.phantazm.mob.skill.Skill;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A store of {@link PhantazmMob}s.
 */
public class MobStore implements Tickable {
    private static final Key DEATH_KEY = Key.key(Namespaces.PHANTAZM, "death");
    private static final Key SPAWN_KEY = Key.key(Namespaces.PHANTAZM, "spawn");

    private final Map<UUID, PhantazmMob> uuidToMob = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Pair<PhantazmMob, Collection<Skill>>> tickableSkills =
            new ConcurrentHashMap<>();

    /**
     * Attempts to activate triggers for an {@link Entity}.
     *
     * @param entity The {@link Entity} to activate triggers for
     * @param key    The {@link Key} of the {@link net.minestom.server.event.Event} that triggered the activation
     */
    public void useTrigger(@NotNull Entity entity, @NotNull Key key) {
        PhantazmMob mob = uuidToMob.get(entity.getUuid());
        if (mob != null) {
            Collection<Skill> triggerInstance = mob.triggers().get(key);
            if (triggerInstance != null) {
                for (Skill skill : triggerInstance) {
                    skill.use(mob);
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

        PhantazmMob mob = uuidToMob.remove(uuid);
        if (mob != null) {
            Collection<Skill> deathSkills = mob.triggers().get(DEATH_KEY);
            if (deathSkills != null) {
                for (Skill skill : deathSkills) {
                    skill.use(mob);
                }
            }
        }

        tickableSkills.remove(uuid);
    }

    /**
     * Registers a {@link PhantazmMob} to the store.
     *
     * @param mob The {@link PhantazmMob} to register
     */
    public void onMobSpawn(@NotNull PhantazmMob mob) {
        Objects.requireNonNull(mob, "mob");

        UUID uuid = mob.entity().getUuid();
        if (uuidToMob.containsKey(uuid)) {
            throw new IllegalArgumentException("Mob with uuid " + uuid + " already registered");
        }

        uuidToMob.put(uuid, mob);

        List<Skill> tickables = new ArrayList<>(3);
        for (Collection<Skill> skills : mob.triggers().values()) {
            for (Skill skill : skills) {
                if (skill.needsTicking()) {
                    tickables.add(skill);
                }
            }
        }

        if (!tickables.isEmpty()) {
            tickableSkills.put(uuid, Pair.of(mob, List.copyOf(tickables)));
        }

        Collection<Skill> spawnSkills = mob.triggers().get(SPAWN_KEY);
        if (spawnSkills != null) {
            for (Skill skill : spawnSkills) {
                skill.use(mob);
            }
        }
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

    @Override
    public void tick(long time) {
        for (Pair<PhantazmMob, Collection<Skill>> tickables : tickableSkills.values()) {
            for (Skill skill : tickables.right()) {
                skill.tick(time, tickables.left());
            }
        }
    }
}
