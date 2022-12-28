package org.phantazm.mob.goal;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.skill.Skill;
import org.phantazm.neuron.bindings.minestom.entity.goal.NeuralGoal;

import java.util.Objects;

/**
 * A {@link NeuralGoal} that periodically uses a {@link Skill}.
 */
@Model("mob.goal.use_skill")
public class UseSkillGoal implements NeuralGoal {

    private final Data data;
    private final Skill skill;
    private long lastUsage = System.currentTimeMillis();

    /**
     * Creates a {@link UseSkillGoal}.
     *
     * @param skill The {@link Skill} to use
     */
    @FactoryMethod
    public UseSkillGoal(@NotNull Data data, @NotNull @Child("skill") Skill skill) {
        this.data = Objects.requireNonNull(data, "data");
        this.skill = Objects.requireNonNull(skill, "skill");
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                String skillPath = element.getStringOrThrow("skillPath");
                long period = element.getNumberOrThrow("period").longValue();

                return new Data(skillPath, period);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) {
                return ConfigNode.of("skillPath", data.skillPath(), "period", data.period());
            }
        };
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
    public void start() {

    }

    @Override
    public void tick(long time) {
        if (time - lastUsage >= data.period()) {
            skill.use();
            lastUsage = time;
        }
    }

    @Override
    public void end() {

    }

    @DataObject
    public record Data(@NotNull @DataPath("skill") String skillPath, long period) {

        public Data {
            Objects.requireNonNull(skillPath, "skillPath");
        }
    }

}
