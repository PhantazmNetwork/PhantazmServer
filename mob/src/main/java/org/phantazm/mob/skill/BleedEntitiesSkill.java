package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.target.TargetSelector;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;

@Model("mob.skill.bleed")
@Cache(false)
public class BleedEntitiesSkill implements Skill {

    private final Collection<BleedContext> bleeding = new LinkedList<>();
    private final Data data;
    private final TargetSelector<? extends LivingEntity> selector;

    @FactoryMethod
    public BleedEntitiesSkill(@NotNull Data data,
            @NotNull @Child("selector") TargetSelector<? extends LivingEntity> selector) {
        this.data = Objects.requireNonNull(data, "data");
        this.selector = Objects.requireNonNull(selector, "selector");
    }

    @Override
    public void use(@NotNull PhantazmMob self) {
        selector.selectTarget(self).ifPresent(livingEntity -> bleeding.add(new BleedContext(livingEntity, 0L)));
    }

    @Override
    public void tick(long time, @NotNull PhantazmMob self) {
        Iterator<BleedContext> contextIterator = bleeding.iterator();
        if (!contextIterator.hasNext()) {
            return;
        }

        for (BleedContext bleedContext = contextIterator.next(); contextIterator.hasNext();
                bleedContext = contextIterator.next()) {
            LivingEntity livingEntity = bleedContext.target();
            if (livingEntity.isRemoved()) {
                contextIterator.remove();
                continue;
            }

            long ticksSinceStart = bleedContext.ticksSinceStart();
            bleedContext.setTicksSinceStart(ticksSinceStart + 1);
            if (ticksSinceStart % data.bleedInterval() == 0) {
                livingEntity.damage(Damage.fromEntity(self.entity(), data.bleedDamage()), data.bypassArmor);
                contextIterator.remove();
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
        Iterator<BleedContext> contextIterator = bleeding.iterator();
        if (!contextIterator.hasNext()) {
            return;
        }

        while (contextIterator.hasNext()) {
            BleedContext next = contextIterator.next();
            if (next.target == self.entity()) {
                contextIterator.remove();
            }
        }
    }

    @DataObject
    public record Data(@NotNull @ChildPath("selector") String selector,
                       float bleedDamage,
                       boolean bypassArmor,
                       long bleedInterval,
                       long bleedTime) {

        public Data {
            Objects.requireNonNull(selector, "selector");
        }

    }

    private static final class BleedContext {

        private final LivingEntity target;
        private long ticksSinceStart;


        public BleedContext(@NotNull LivingEntity target, long ticksSinceStart) {
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
