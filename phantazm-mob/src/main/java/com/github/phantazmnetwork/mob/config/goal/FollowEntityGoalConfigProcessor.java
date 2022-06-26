package com.github.phantazmnetwork.mob.config.goal;

import com.github.phantazmnetwork.mob.goal.FollowEntityGoal;
import com.github.phantazmnetwork.mob.target.TargetSelector;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class FollowEntityGoalConfigProcessor<TEntity extends Entity> implements ConfigProcessor<FollowEntityGoal<TEntity>> {

    private final ConfigProcessor<? extends TargetSelector<TEntity>> targetSelectorConfigProcessor;

    public FollowEntityGoalConfigProcessor(@NotNull ConfigProcessor<? extends TargetSelector<TEntity>> targetSelectorConfigProcessor) {
        this.targetSelectorConfigProcessor = Objects.requireNonNull(targetSelectorConfigProcessor,
                "targetSelectorConfigProcessor");
    }

    @Override
    public FollowEntityGoal<TEntity> dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        TargetSelector<TEntity> targetSelector = targetSelectorConfigProcessor
                .dataFromElement(element.getElement("targetSelector"));
        return new FollowEntityGoal<>(targetSelector);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull ConfigElement elementFromData(@NotNull FollowEntityGoal<TEntity> followEntityGoal) throws ConfigProcessException {
        ConfigNode node = new LinkedConfigNode();
        ConfigProcessor<TargetSelector<TEntity>> genericProcessor = (ConfigProcessor<TargetSelector<TEntity>>) targetSelectorConfigProcessor;
        node.put("targetSelector", genericProcessor.elementFromData(followEntityGoal.entitySelector()));

        return node;
    }
}
