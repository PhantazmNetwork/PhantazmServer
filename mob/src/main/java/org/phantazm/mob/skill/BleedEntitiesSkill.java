package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.DamageType;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.target.TargetSelector;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;

@Model("mob.skill.bleed")
public class BleedEntitiesSkill implements Skill {

    private final Collection<BleedContext> bleeding = new LinkedList<>();
    private final Data data;
    private final DamageType damageType;
    private final TargetSelector<? extends LivingEntity> selector;

    @FactoryMethod
    public BleedEntitiesSkill(@NotNull Data data, @NotNull Entity user,
            @NotNull @Child("selector") TargetSelector<? extends LivingEntity> selector) {
        this.data = Objects.requireNonNull(data, "data");
        this.damageType = DamageType.fromEntity(Objects.requireNonNull(user, "user"));
        this.selector = Objects.requireNonNull(selector, "selector");
    }

    @Override
    public void use() {
        selector.selectTarget().ifPresent(livingEntity -> {
            bleeding.add(new BleedContext(livingEntity, 0L));
        });
    }

    @Override
    public void tick(long time) {
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
                livingEntity.damage(damageType, data.bleedDamage());
                contextIterator.remove();
            }
            if (ticksSinceStart >= data.bleedTime()) {
                contextIterator.remove();
            }
        }
    }

    @DataObject
    public record Data(@NotNull @ChildPath("selector") String selectorPath,
                       float bleedDamage,
                       long bleedInterval,
                       long bleedTime) {

        public Data {
            Objects.requireNonNull(selectorPath, "selectorPath");
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
