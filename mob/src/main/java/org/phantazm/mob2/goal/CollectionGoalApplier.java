package org.phantazm.mob2.goal;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.proxima.bindings.minestom.goal.ProximaGoal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Model("mob.goal_applier.collection")
@Cache
public class CollectionGoalApplier implements GoalApplier {
    private final Collection<GoalCreator> creators;

    @FactoryMethod
    public CollectionGoalApplier(@NotNull @Child("goalCreators") Collection<GoalCreator> creators) {
        this.creators = List.copyOf(creators);
    }

    @Override
    public void apply(@NotNull Mob mob) {
        Collection<ProximaGoal> goalCollection = new ArrayList<>(creators.size());
        for (GoalCreator creator : creators) {
            goalCollection.add(creator.create(mob));
        }

        mob.addGoalGroup(new CollectionGoalGroup(goalCollection));
    }
}
