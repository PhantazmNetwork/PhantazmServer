package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.MobModel;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.spawner.MobSpawner;

import java.util.Objects;

@Model("mob.skill.duplicate_self")
public class DuplicateSelfSkill implements Skill {

    private final MobStore mobStore;

    private final MobSpawner spawner;

    private final MobModel model;

    private final Entity entity;

    @FactoryMethod
    public DuplicateSelfSkill(@NotNull MobStore mobStore, @NotNull MobSpawner spawner, @NotNull MobModel model,
            @NotNull Entity entity) {
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
        this.spawner = Objects.requireNonNull(spawner, "spawner");
        this.model = Objects.requireNonNull(model, "model");
        this.entity = Objects.requireNonNull(entity, "entity");
    }

    @Override
    public void use() {
        Instance instance = entity.getInstance();
        if (instance == null) {
            return;
        }

        spawner.spawn(instance, entity.getPosition(), mobStore, model);
    }

    @Override
    public void tick(long time) {

    }
}