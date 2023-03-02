package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.spawner.MobSpawner;

import java.util.Objects;

@Model("mob.skill.duplicate_self")
@Cache(false)
public class DuplicateSelfSkill implements Skill {
    private final MobSpawner spawner;

    @FactoryMethod
    public DuplicateSelfSkill(@NotNull MobSpawner spawner) {
        this.spawner = Objects.requireNonNull(spawner, "spawner");
    }

    @Override
    public void use(@NotNull PhantazmMob self) {
        Entity entity = self.entity();
        Instance instance = entity.getInstance();
        if (instance == null) {
            return;
        }

        spawner.spawn(instance, entity.getPosition(), self.model());
    }

    @Override
    public void tick(long time, @NotNull PhantazmMob self) {

    }
}
