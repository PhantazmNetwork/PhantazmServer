package com.github.phantazmnetwork.mob.goal;

import com.github.phantazmnetwork.mob.skill.Skill;
import com.github.phantazmnetwork.mob.target.LastHitSelector;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntity;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.NeuralGoal;
import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

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
    public MeleeAttackGoal(@NotNull Data data, @NotNull @DataName("skills") Collection<Skill> skills,
            @NotNull @DataName("last_hit_selector") LastHitSelector<LivingEntity> lastHitSelector,
            @NotNull @Dependency("mob.entity.neural_entity") NeuralEntity entity) {
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
    public record Data(@NotNull @DataPath("skills") String skillPaths,
                       @NotNull @DataPath("last_hit_selector") String lastHitSelectorPath,
                       long cooldown,
                       double rangeSquared) {

        public Data {
            Objects.requireNonNull(lastHitSelectorPath, "lastHitSelectorPath");
        }

    }
}
