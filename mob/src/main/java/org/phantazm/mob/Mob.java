package org.phantazm.mob;

import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.skill.Skill;
import org.phantazm.proxima.bindings.minestom.Pathfinding;
import org.phantazm.proxima.bindings.minestom.ProximaEntity;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class Mob extends ProximaEntity {
    private final List<Skill> skills;

    public Mob(@NotNull EntityType entityType, @NotNull UUID uuid, @NotNull Pathfinding pathfinding) {
        super(entityType, uuid, pathfinding);
        this.skills = new CopyOnWriteArrayList<>();
    }

    public void addAllSkills(@NotNull Skill skill) {
        
    }

    @Override
    public void update(long time) {
        super.update(time);

    }
}
