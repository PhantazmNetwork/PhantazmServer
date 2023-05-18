package org.phantazm.mob;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Tickable;
import org.phantazm.mob.skill.Skill;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A store of {@link PhantazmMob}s.
 */
public class MobStore implements Tickable {
    private final Map<UUID, PhantazmMob> uuidToMob = new ConcurrentHashMap<>();

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
        uuidToMob.remove(uuid);
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
        for (PhantazmMob mob : uuidToMob.values()) {
            for (Collection<Skill> triggerSkills : mob.triggers().values()) {
                for (Skill skill : triggerSkills) {
                    skill.tick(time, mob);
                }
            }
        }
    }
}
