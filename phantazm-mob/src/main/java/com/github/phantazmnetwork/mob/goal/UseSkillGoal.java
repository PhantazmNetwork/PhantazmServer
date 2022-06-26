package com.github.phantazmnetwork.mob.goal;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.mob.skill.Skill;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.NeuralGoal;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record UseSkillGoal(@NotNull Skill skill, long period) implements GoalCreator, VariantSerializable {

    public final static String SERIAL_NAME = "useSkillGoal";

    public UseSkillGoal(@NotNull Skill skill, long period) {
        this.skill = Objects.requireNonNull(skill, "skill");
        this.period = period;
    }

    @Override
    public @NotNull NeuralGoal createGoal(@NotNull PhantazmMob mob) {
        return new NeuralGoal() {

            private long lastUsage = System.currentTimeMillis();

            @Override
            public boolean shouldStart() {
                return true;
            }

            @Override
            public void start() {

            }

            @Override
            public boolean shouldEnd() {
                return false;
            }

            @Override
            public void end() {

            }

            @Override
            public void tick(long time) {
                if (time - lastUsage >= period) {
                    skill.use(mob);
                    lastUsage = time;
                }
            }
        };
    }

    @Override
    public @NotNull String getSerialName() {
        return SERIAL_NAME;
    }
}
