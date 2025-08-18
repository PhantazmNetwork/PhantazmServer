package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.core.particle.ParticleWrapper;
import org.phantazm.mob2.Mob;
import org.phantazm.core.Target;
import org.phantazm.mob2.Trigger;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

import java.util.Objects;

@Model("mob.skill.spawn_particle_circle")
@Cache
public class SpawnParticleCircleSkill implements SkillComponent {
    private final SpawnParticleCircleSkill.Data data;
    private final ParticleWrapper particle;
    private final SelectorComponent selector;

    @FactoryMethod
    public SpawnParticleCircleSkill(@NotNull SpawnParticleCircleSkill.Data data, @NotNull @Child("particle") ParticleWrapper particle,
        @NotNull @Child("selector") SelectorComponent selector) {
        this.data = Objects.requireNonNull(data);
        this.particle = Objects.requireNonNull(particle);
        this.selector = Objects.requireNonNull(selector);

        if (this.data.density == 0.0) {
            throw new IllegalArgumentException("Density is zero in a spawn_particle_circle invocation.");
        }
    }

    @Override
    public @NotNull Skill get() {
        return new SpawnParticleCircleSkill.Internal(selector.get(), data, particle);
    }

    @Default("""
        {
          trigger=null,
          heightOffset=0.0,
          radius=0.0,
          density=1.0
        }
        """)
    @DataObject
    public record Data(
        @Nullable Trigger trigger,
        double heightOffset,
        double radius,
        double density) {
    }

    private static class Internal extends TargetedSkill {
        private final SpawnParticleCircleSkill.Data data;
        private final ParticleWrapper particle;

        public Internal(Selector selector, SpawnParticleCircleSkill.Data data, ParticleWrapper particle) {
            super(selector);
            this.data = data;
            this.particle = particle;
        }

        @Override
        protected void useOnTarget(@NotNull Target target, @NotNull Mob mob) {
            Instance instance = mob.getInstance();
            if (instance == null) {
                return;
            }

            for (Point position : target.locations()) {
                // position = all selected centers?
                // these must all be cast to integer due to how Bresenham's algorithm works, i.e. the points are based
                // on integers as this was originally intended for a grid of pixels
                int xCenter = (int) Math.floor(position.x() * data.density); // part of center
                int zCenter = (int) Math.floor(position.z() * data.density); // part of center
                double y = position.y() + data.heightOffset;

                double decision = 3 - (2 * data.radius);
                int x = 0;

                // z represents iterations
                // allow only 1 decimal place for the sake of easy implementation?
                int z = (int) Math.floor(data.radius * data.density);

                while (z >= x) {
                    if (decision > 0) {
                        z--;
                        decision = decision + (4 * (x - z)) + 10;
                    } else {
                        decision = decision + (4 * x) + 6;
                    }
                    x++;
                    drawAllOctets(y, xCenter, zCenter, x, z, instance, particle);
                }
            }

        }

        private void drawAllOctets(double y, int xCenter, int zCenter, int x, int z, Instance instance, ParticleWrapper particle) {
            particle.sendTo(instance, (double) (xCenter + x) / data.density, y, (double) (zCenter + z) / data.density);
            particle.sendTo(instance, (double) (xCenter - x) / data.density, y, (double) (zCenter + z) / data.density);
            particle.sendTo(instance, (double) (xCenter + x) / data.density, y, (double) (zCenter - z) / data.density);
            particle.sendTo(instance, (double) (xCenter - x) / data.density, y, (double) (zCenter - z) / data.density);
            particle.sendTo(instance, (double) (xCenter + z) / data.density, y, (double) (zCenter + x) / data.density);
            particle.sendTo(instance, (double) (xCenter - z) / data.density, y, (double) (zCenter + x) / data.density);
            particle.sendTo(instance, (double) (xCenter + z) / data.density, y, (double) (zCenter - x) / data.density);
            particle.sendTo(instance, (double) (xCenter - z) / data.density, y, (double) (zCenter - x) / data.density);
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }
    }
}
