package com.github.phantazmnetwork.mob.config.goal;

import com.github.phantazmnetwork.mob.goal.UseSkillGoal;
import com.github.phantazmnetwork.mob.skill.Skill;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link ConfigProcessor} for {@link UseSkillGoal}s.
 */
public class UseSkillGoalConfigProcessor implements ConfigProcessor<UseSkillGoal> {

    private final ConfigProcessor<Skill> skillProcessor;

    /**
     * Creates a new {@link UseSkillGoalConfigProcessor}.
     * @param skillProcessor A {@link ConfigProcessor} for {@link Skill}s
     */
    public UseSkillGoalConfigProcessor(@NotNull ConfigProcessor<Skill> skillProcessor) {
        this.skillProcessor = Objects.requireNonNull(skillProcessor, "skillProcessor");
    }

    @Override
    public UseSkillGoal dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        Skill skill = skillProcessor.dataFromElement(element.getElementOrThrow("skill"));
        long period = element.getNumberOrThrow("period").longValue();

        return new UseSkillGoal(skill, period);
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull UseSkillGoal useSkillGoal) throws ConfigProcessException {
        ConfigNode configNode = new LinkedConfigNode(2);
        configNode.put("skill", skillProcessor.elementFromData(useSkillGoal.skill()));
        configNode.putNumber("period", useSkillGoal.period());

        return configNode;
    }
}
