package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.target.TargetSelector;
import org.phantazm.mob.validator.TargetValidator;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;

@Model("mob.skill.bleed")
@Cache(false)
public class BleedEntitiesSkill implements Skill {

    private final Collection<BleedContext> bleeding = new LinkedList<>();
    private final Data data;
    private final TargetSelector<?> selector;
    private final TargetValidator validator;

    @FactoryMethod
    public BleedEntitiesSkill(@NotNull Data data, @NotNull @Child("selector") TargetSelector<?> selector,
            @NotNull @Child("validator") TargetValidator validator) {
        this.data = Objects.requireNonNull(data, "data");
        this.selector = Objects.requireNonNull(selector, "selector");
        this.validator = Objects.requireNonNull(validator, "validator");
    }

    @Override
    public void use(@NotNull PhantazmMob self) {
        selector.selectTarget(self).ifPresent(target -> {
            if (target instanceof LivingEntity livingEntity) {
                bleeding.add(new BleedContext(self, livingEntity, 0L));
            }
            else if (target instanceof Iterable<?> iterable) {
                for (Object object : iterable) {
                    if (object instanceof LivingEntity livingEntity) {
                        bleeding.add(new BleedContext(self, livingEntity, 0L));
                    }
                }
            }
        });
    }

    @Override
    public void tick(long time, @NotNull PhantazmMob self) {
        Iterator<BleedContext> contextIterator = bleeding.iterator();
        while (contextIterator.hasNext()) {
            BleedContext bleedContext = contextIterator.next();
            if (bleedContext.self != self) {
                continue;
            }

            LivingEntity livingEntity = bleedContext.target();
            if (livingEntity.isRemoved()) {
                contextIterator.remove();
                continue;
            }

            if (validator.valid(self.entity(), livingEntity)) {
                contextIterator.remove();
                continue;
            }

            long ticksSinceStart = bleedContext.ticksSinceStart();
            bleedContext.setTicksSinceStart(ticksSinceStart + 1);
            if (ticksSinceStart % data.bleedInterval() == 0) {
                livingEntity.damage(Damage.fromEntity(self.entity(), data.bleedDamage()), data.bypassArmor);
            }

            if (ticksSinceStart >= data.bleedTime()) {
                contextIterator.remove();
            }
        }
    }

    @Override
    public boolean needsTicking() {
        return true;
    }

    @Override
    public void end(@NotNull PhantazmMob self) {
        bleeding.removeIf(next -> next.self == self);
    }

    @DataObject
    public record Data(@NotNull @ChildPath("selector") String selector,
                       @NotNull @ChildPath("validator") String validator,
                       float bleedDamage,
                       boolean bypassArmor,
                       long bleedInterval,
                       long bleedTime) {

        public Data {
            Objects.requireNonNull(selector, "selector");
        }

    }

    private static final class BleedContext {
        private final PhantazmMob self;
        private final LivingEntity target;
        private long ticksSinceStart;


        public BleedContext(@NotNull PhantazmMob self, @NotNull LivingEntity target, long ticksSinceStart) {
            this.self = self;
            this.target = Objects.requireNonNull(target, "target");
            this.ticksSinceStart = ticksSinceStart;
        }

        public @NotNull LivingEntity target() {
            return target;
        }

        public long ticksSinceStart() {
            return ticksSinceStart;
        }

        public void setTicksSinceStart(long ticksSinceStart) {
            this.ticksSinceStart = ticksSinceStart;
        }

    }
}
