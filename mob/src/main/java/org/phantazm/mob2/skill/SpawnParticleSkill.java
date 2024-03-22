package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import com.github.steanky.vector.Bounds3D;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.core.particle.ParticleWrapper;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.Trigger;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

import java.util.Objects;
import java.util.Random;

@Model("mob.skill.spawn_particle")
@Cache
public class SpawnParticleSkill implements SkillComponent {
    private final Data data;
    private final ParticleWrapper particle;
    private final SelectorComponent selector;
    private final Random random;

    @FactoryMethod
    public SpawnParticleSkill(@NotNull Data data, @NotNull @Child("particle") ParticleWrapper particle,
        @NotNull @Child("selector") SelectorComponent selector) {
        this.data = Objects.requireNonNull(data);
        this.particle = Objects.requireNonNull(particle);
        this.selector = Objects.requireNonNull(selector);
        this.random = new Random();
    }

    @Override
    public @NotNull Skill apply(@NotNull ExtensionHolder holder) {
        return new Internal(selector.apply(holder), data, particle, random);
    }

    @Default("""
        {
          trigger=null
        }
        """)
    @DataObject
    public record Data(
        @Nullable Trigger trigger,
        @NotNull Bounds3D bounds) {
    }

    private static class Internal extends TargetedSkill {
        private final Data data;
        private final ParticleWrapper particle;
        private final Random random;

        public Internal(Selector selector, Data data, ParticleWrapper particle, Random random) {
            super(selector);
            this.data = data;
            this.particle = particle;
            this.random = random;
        }

        @Override
        protected void useOnTarget(@NotNull Target target, @NotNull Mob mob) {
            Instance instance = mob.getInstance();
            if (instance == null) {
                return;
            }

            for (Point position : target.locations()) {
                double x = position.x() + data.bounds().originX() + getOffset(data.bounds().lengthX());
                double y = position.y() + data.bounds().originY() + getOffset(data.bounds().lengthY());
                double z = position.z() + data.bounds().originZ() + getOffset(data.bounds().lengthZ());

                particle.sendTo(instance, x, y, z);
            }
        }

        private double getOffset(double length) {
            if (length <= 0) {
                return 0;
            }

            return random.nextDouble(length);
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }
    }
}
