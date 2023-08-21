package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Keys;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.MobSpawner;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.goal.CollectionGoalGroup;
import org.phantazm.mob2.goal.ProjectileMovementGoal;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;
import org.phantazm.mob2.validator.Validator;
import org.phantazm.mob2.validator.ValidatorComponent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Model("mob.skill.shoot_projectile")
@Cache
public class ShootProjectileSkill implements SkillComponent {
    private final Data data;

    private final SelectorComponent targetSelector;
    private final ValidatorComponent hitValidator;
    private final SpawnCallbackComponent callback;

    @FactoryMethod
    public ShootProjectileSkill(@NotNull Data data, @NotNull SelectorComponent targetSelector,
        @NotNull ValidatorComponent hitValidator, @NotNull SpawnCallbackComponent callback) {
        this.data = Objects.requireNonNull(data);
        this.targetSelector = Objects.requireNonNull(targetSelector);
        this.hitValidator = Objects.requireNonNull(hitValidator);
        this.callback = Objects.requireNonNull(callback);
    }

    @Override
    public @NotNull Skill apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Internal(data, mob, targetSelector.apply(mob, injectionStore), hitValidator.apply(mob,
            injectionStore), callback.apply(mob, injectionStore), injectionStore.get(Keys.MOB_SPAWNER));
    }

    @DataObject
    public record Data(@NotNull @ChildPath("target_selector") String targetSelector,
        @NotNull @ChildPath("hit_validator") String hitValidator,
        @NotNull @ChildPath("callback") String callback,
        @NotNull Key entity,
        double power,
        double spread,
        boolean gravity) {
        @Default("spread")
        public static @NotNull ConfigElement defaultSpread() {
            return ConfigPrimitive.of(0.0D);
        }

        @Default("gravity")
        public static @NotNull ConfigElement defaultGravity() {
            return ConfigPrimitive.of(true);
        }
    }

    private record Internal(Data data,
        Mob self,
        Selector targetSelector,
        Validator hitValidator,
        SpawnCallback callback,
        MobSpawner spawner) implements Skill {

        private void onCollideWithBlock(ProjectileCollideWithBlockEvent event) {
            killOrRemove(event.getEntity());
        }

        private void onCollideWithEntity(ProjectileCollideWithEntityEvent event) {
            if (!hitValidator.valid(event.getTarget())) {
                return;
            }

            self.attack(event.getTarget());
            killOrRemove(event.getEntity());
        }

        private void killOrRemove(Entity entity) {
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.kill();
            } else {
                entity.remove();
            }
        }

        @Override
        public void use() {
            Instance instance = self.getInstance();
            if (instance == null) {
                return;
            }

            Target target = targetSelector.select();
            Optional<? extends Entity> targetEntityOptional = target.target();
            if (targetEntityOptional.isPresent()) {
                Entity targetEntity = targetEntityOptional.get();
                if (!hitValidator.valid(targetEntity)) {
                    return;
                }

                shootAtPoint(instance, targetEntity.getPosition()
                    .add(0, targetEntity.getBoundingBox().height() / 2, 0));
                return;
            }

            Optional<? extends Point> targetPointOptional = target.location();
            if (targetPointOptional.isEmpty()) {
                return;
            }

            shootAtPoint(instance, targetPointOptional.get());
        }

        private void shootAtPoint(Instance instance, Point target) {
            Mob mob = spawner.spawn(data.entity, instance, self.getPosition().add(0, self.getEyeHeight(), 0));
            callback.accept(mob);

            mob.setNoGravity(!data.gravity);
            mob.addGoalGroup(new CollectionGoalGroup(List.of(new ProjectileMovementGoal(mob, self, target, data.power(),
                data.spread(), this::onCollideWithBlock, this::onCollideWithEntity))));
        }
    }
}
