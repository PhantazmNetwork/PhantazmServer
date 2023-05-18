package org.phantazm.mob.goal;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.proxima.bindings.minestom.goal.ProximaGoal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Model("mob.goal_applier.collection")
@Cache(false)
public class CollectionGoalApplier implements GoalApplier {
    private final Collection<GoalCreator> creators;

    @FactoryMethod
    public CollectionGoalApplier(@NotNull @Child("goal_creators") Collection<GoalCreator> creators) {
        this.creators = List.copyOf(creators);
    }

    @Override
    public void apply(@NotNull PhantazmMob mob) {
        Collection<ProximaGoal> goalCollection = new ArrayList<>(creators.size());
        for (GoalCreator creator : creators) {
            goalCollection.add(creator.create(mob));
        }

        mob.entity().addGoalGroup(new CollectionGoalGroup(goalCollection));
    }

    @DataObject
    public record Data(@NotNull @ChildPath("goal_creators") List<String> goalCreators) {
    }
}
