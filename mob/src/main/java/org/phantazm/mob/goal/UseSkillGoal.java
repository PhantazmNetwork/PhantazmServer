package org.phantazm.mob.goal;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.skill.Skill;
import org.phantazm.proxima.bindings.minestom.goal.ProximaGoal;

import java.util.Objects;

/**
 * A {@link ProximaGoal} that periodically uses a {@link Skill}.
 */
@Model("mob.goal.use_skill")
public class UseSkillGoal implements ProximaGoal {

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
        if ((time - lastUsage) / MinecraftServer.TICK_MS >= data.period()) {
            skill.use();
            lastUsage = time;
        }
    }

    @Override
    public void end() {

    }

    @DataObject
    public record Data(@NotNull @ChildPath("skill") String skillPath, long period) {

        public Data {
            Objects.requireNonNull(skillPath, "skillPath");
        }
    }

}
