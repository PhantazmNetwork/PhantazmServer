package com.github.phantazmnetwork.mob.goal;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.mob.skill.SkillInstance;
import com.github.phantazmnetwork.mob.skill.Skill;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.NeuralGoal;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record UseSkillGoal(@NotNull Skill skill, long period) implements Goal {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "goal.use_skill");

    public UseSkillGoal(@NotNull Skill skill, long period) {
        this.skill = Objects.requireNonNull(skill, "skill");
        this.period = period;
    }

    @Override
    public @NotNull NeuralGoal createGoal(@NotNull PhantazmMob mob) {
        return new NeuralGoal() {

            private final SkillInstance skillInstance = skill().createSkill(mob);

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
                    skillInstance.use();
                    lastUsage = time;
                }
            }
        };
    }

    @Override
    public @NotNull Key key() {
        return SERIAL_KEY;
    }
}
