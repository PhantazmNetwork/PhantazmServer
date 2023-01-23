package org.phantazm.mob.goal;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.skill.Skill;
import org.phantazm.mob.target.LastHitSelector;
import org.phantazm.neuron.bindings.minestom.entity.NeuralEntity;
import org.phantazm.neuron.bindings.minestom.entity.goal.NeuralGoal;

import java.util.Collection;
import java.util.Objects;

@Model("mob.goal.melee_attack")
public class MeleeAttackGoal implements NeuralGoal {

    private final Data data;
    private final Collection<Skill> skills;
    private final LastHitSelector<LivingEntity> lastHitSelector;
    private final NeuralEntity entity;
    private long ticksSinceLastAttack = 0L;

    @FactoryMethod
    public MeleeAttackGoal(@NotNull Data data, @NotNull @Child("skills") Collection<Skill> skills,
            @NotNull @Child("last_hit_selector") LastHitSelector<LivingEntity> lastHitSelector,
            @NotNull NeuralEntity entity) {
        this.data = Objects.requireNonNull(data, "data");
        this.skills = Objects.requireNonNull(skills, "skills");
        this.lastHitSelector = Objects.requireNonNull(lastHitSelector, "lastHitSelector");
        this.entity = Objects.requireNonNull(entity, "entity");
    }

    @Override
    public boolean shouldStart() {
        return true;
    }

    @Override
    public boolean shouldEnd() {
        return false;
    }

    @Override
    public void tick(long time) {
        if (ticksSinceLastAttack >= data.cooldown()) {
            Entity target = entity.getTarget();
            if (target == null) {
                return;
            }

            double distance = entity.getDistanceSquared(target);
            if (distance <= data.rangeSquared()) {
                entity.attack(target, true);
                if (target instanceof LivingEntity livingEntity) {
                    lastHitSelector.setLastHit(livingEntity);

                    for (Skill skill : skills) {
                        skill.use();
                    }
                }
                ticksSinceLastAttack = 0L;
            }
        }
        else {
            ticksSinceLastAttack++;
        }
    }

    @DataObject
    public record Data(@NotNull @ChildPath("skills") String skillPaths,
                       @NotNull @ChildPath("last_hit_selector") String lastHitSelectorPath,
                       long cooldown,
                       double rangeSquared) {

        public Data {
            Objects.requireNonNull(lastHitSelectorPath, "lastHitSelectorPath");
        }

    }
}
