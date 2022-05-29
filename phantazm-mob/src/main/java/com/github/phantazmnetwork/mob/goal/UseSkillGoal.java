package com.github.phantazmnetwork.mob.goal;

import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.mob.skill.Skill;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.NeuralGoal;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class UseSkillGoal implements NeuralGoal {

    private final PhantazmMob mob;

    private final Skill skill;

    private final long period;

    private long lastUsage = System.currentTimeMillis();

    public UseSkillGoal(@NotNull PhantazmMob mob, @NotNull Skill skill, long period) {
        this.mob = Objects.requireNonNull(mob, "mob");
        this.skill = Objects.requireNonNull(skill, "skill");
        this.period = period;
    }

    @Override
    public void tick(long time) {
        if (time - lastUsage > period) {
            skill.use(mob);
            lastUsage = time;
        }
    }

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
}
