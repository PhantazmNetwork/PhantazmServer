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

/**
 * A {@link ConfigProcessor} for {@link FollowEntityGoal}s.
 * @param <TEntity> The goal's type of {@link Entity}
 */
public abstract class FollowEntityGoalConfigProcessor<TEntity extends Entity> implements ConfigProcessor<FollowEntityGoal<TEntity>> {

    private final ConfigProcessor<TargetSelector<TEntity>> selectorProcessor;

    /**
     * Creates a {@link FollowEntityGoalConfigProcessor}.
     * @param selectorProcessor A {@link ConfigProcessor} for {@link TEntity} {@link TargetSelector}s
     */
    public FollowEntityGoalConfigProcessor(@NotNull ConfigProcessor<TargetSelector<TEntity>> selectorProcessor) {
        this.selectorProcessor = Objects.requireNonNull(selectorProcessor, "selectorProcessor");
    }

    @Override
    public FollowEntityGoal<TEntity> dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        TargetSelector<TEntity> targetSelector = selectorProcessor.dataFromElement(element.getElement("selector"));
        return createGoal(targetSelector);
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull FollowEntityGoal<TEntity> followEntityGoal) throws ConfigProcessException {
        ConfigNode node = new LinkedConfigNode(1);
        node.put("selector", selectorProcessor.elementFromData(followEntityGoal.getSelector()));

        return node;
    }

    /**
     * Creates a {@link FollowEntityGoal} from a {@link TargetSelector}.
     * @param selector The {@link TargetSelector} to use
     * @return A new {@link FollowEntityGoal}
     */
    protected abstract @NotNull FollowEntityGoal<TEntity> createGoal(@NotNull TargetSelector<TEntity> selector);

}
