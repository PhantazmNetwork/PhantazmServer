package org.phantazm.mob2;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.skill.Skill;
import org.phantazm.mob2.trigger.Trigger;
import org.phantazm.proxima.bindings.minestom.Pathfinding;
import org.phantazm.proxima.bindings.minestom.ProximaEntity;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class Mob extends ProximaEntity {
    private final Set<Skill> tickableSkills;
    private final Map<String, Set<Skill>> mappedSkills;

    public Mob(@NotNull EntityType entityType, @NotNull UUID uuid, @NotNull Pathfinding pathfinding) {
        super(entityType, uuid, pathfinding);
        this.tickableSkills = new CopyOnWriteArraySet<>();
        this.mappedSkills = new ConcurrentHashMap<>();
    }

    public void addSkills(@NotNull Collection<? extends Skill.@NotNull Entry> entries) {
        Objects.requireNonNull(entries);
        List<Skill> tickableSkills = new ArrayList<>(entries.size());

        for (Skill.Entry entry : entries) {
            addSkill0(entry, tickableSkills, this.mappedSkills);
        }

        this.tickableSkills.addAll(tickableSkills);
    }

    public void addSkill(Skill.@NotNull Entry entry) {
        Objects.requireNonNull(entry);
        addSkill0(entry, this.tickableSkills, this.mappedSkills);
    }

    public void removeSkill(Skill.@NotNull Entry entry) {
        Objects.requireNonNull(entry);
        Skill skill = entry.skill();
        String trigger = entry.trigger();

        tickableSkills.remove(skill);
        if (trigger == null) {
            return;
        }

        Set<Skill> triggers = mappedSkills.get(trigger);
        if (triggers != null) {
            triggers.remove(skill);
        }
    }

    private static void addSkill0(Skill.Entry entry, Collection<Skill> tickableSkills,
            Map<String, Set<Skill>> mappedSkills) {
        Skill skill = entry.skill();
        String trigger = entry.trigger();

        boolean needsTicking = skill.needsTicking();
        if (!needsTicking && trigger == null) {
            return;
        }

        if (needsTicking) {
            tickableSkills.add(skill);
        }

        if (trigger != null) {
            mappedSkills.computeIfAbsent(trigger, ignored -> new CopyOnWriteArraySet<>()).add(skill);
        }
    }

    private void useIfPresent(String trigger) {
        Set<Skill> skills = mappedSkills.get(trigger);
        if (skills == null) {
            return;
        }

        for (Skill skill : skills) {
            skill.use();
        }
    }

    @Override
    public boolean damage(@NotNull Damage damage, boolean bypassArmor) {
        boolean result = super.damage(damage, bypassArmor);
        useIfPresent(Trigger.DAMAGED);
        return result;
    }

    @Override
    public void attack(@NotNull Entity target, boolean swingHand) {
        super.attack(target, swingHand);
        useIfPresent(Trigger.ATTACK);
    }

    @Override
    public CompletableFuture<Void> setInstance(@NotNull Instance instance, @NotNull Pos spawnPosition) {
        return super.setInstance(instance, spawnPosition).thenAccept((ignored) -> useIfPresent(Trigger.SPAWN));
    }

    @Override
    public void kill() {
        useIfPresent(Trigger.DEATH);
        super.kill();
    }

    @Override
    public void update(long time) {
        super.update(time);

        for (Skill skill : tickableSkills) {
            skill.tick();
        }
    }
}
