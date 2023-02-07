package org.phantazm.mob.goal;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.skill.Skill;
import org.phantazm.proxima.bindings.minestom.ProximaEntity;
import org.phantazm.proxima.bindings.minestom.goal.ProximaGoal;

import java.util.Objects;

/**
 * A {@link ProximaGoal} that periodically uses a {@link Skill}.
 */
@Model("mob.goal.use_skill")
@Cache
public class UseSkillGoal implements GoalCreator {
    private final Data data;
    private final Skill skill;

    @FactoryMethod
    public UseSkillGoal(@NotNull Data data, @NotNull @Child("skill") Skill skill) {
        this.data = Objects.requireNonNull(data, "data");
        this.skill = Objects.requireNonNull(skill, "skill");
    }

    @Override
    public @NotNull ProximaGoal create(@NotNull PhantazmMob mob) {
        return new Goal(data, skill, mob);
    }


    private static class Goal implements ProximaGoal {
        private final Data data;
        private final Skill skill;
        private final PhantazmMob self;

        private long lastUsage;

        public Goal(@NotNull Data data, @NotNull Skill skill, @NotNull PhantazmMob self) {
            this.data = Objects.requireNonNull(data, "data");
            this.skill = Objects.requireNonNull(skill, "skill");
            this.self = Objects.requireNonNull(self, "self");
            this.lastUsage = -1;
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
            if (lastUsage == -1) {
                lastUsage = time;
                return;
            }

            if ((time - lastUsage) / MinecraftServer.TICK_MS >= data.period()) {
                skill.use(self);
                lastUsage = time;
            }
        }

        @Override
        public void end() {

        }
    }

    @DataObject
    public record Data(@NotNull @ChildPath("skill") String skillPath, long period) {

        public Data {
            Objects.requireNonNull(skillPath, "skillPath");
        }
    }

}
