package org.phantazm.mob2;

import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.phantazm.proxima.bindings.minestom.Pathfinding;
import org.phantazm.proxima.bindings.minestom.ProximaEntity;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Mob extends ProximaEntity {
    private final List<Skill> skills;
    private final List<Skill> tickableSkills;

    public Mob(@NotNull EntityType entityType, @NotNull UUID uuid, @NotNull Pathfinding pathfinding) {
        super(entityType, uuid, pathfinding);
        this.skills = new CopyOnWriteArrayList<>();
        this.tickableSkills = new CopyOnWriteArrayList<>();
    }

    public void addSkills(@NotNull Collection<? extends @NotNull Skill> skills) {
        List<Skill> newSkills = List.copyOf(skills);

        this.skills.addAll(newSkills);

        List<Skill> tickables = new ArrayList<>(newSkills.size());
        for (Skill skill : newSkills) {
            if (skill.needsTicking()) {
                tickables.add(skill);
            }
        }

        if (tickables.isEmpty()) {
            return;
        }

        tickableSkills.addAll(tickables);

        for (Skill skill : skills) {
            skill.init();
        }
    }

    public void addSkill(@NotNull Skill skill) {
        Objects.requireNonNull(skill, "skill");

        skills.add(skill);
        if (skill.needsTicking()) {
            tickableSkills.add(skill);
        }

        skill.init();
    }

    @Override
    public void update(long time) {
        super.update(time);

        for (Skill skill : tickableSkills) {
            skill.tick(time);
        }
    }
}
