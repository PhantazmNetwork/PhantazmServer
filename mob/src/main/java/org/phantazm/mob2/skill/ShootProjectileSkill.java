package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.*;
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
    public ShootProjectileSkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent targetSelector,
        @NotNull @Child("hitValidator") ValidatorComponent hitValidator,
        @NotNull @Child("callback") SpawnCallbackComponent callback) {
        this.data = Objects.requireNonNull(data);
        this.targetSelector = Objects.requireNonNull(targetSelector);
        this.hitValidator = Objects.requireNonNull(hitValidator);
        this.callback = Objects.requireNonNull(callback);
    }

    @Override
    public @NotNull Skill apply(@NotNull ExtensionHolder holder) {
        return new Internal(data, targetSelector.apply(holder), hitValidator.apply(holder), callback.apply(holder));
    }

    @Default("""
        {
          trigger=null,
          spread=0.0,
          gravity=true
        }
        """)
    @DataObject
    public record Data(@Nullable Trigger trigger,
        @NotNull Key entity,
        double power,
        double spread,
        boolean gravity) {
    }

    private record Internal(Data data,
        Selector targetSelector,
        Validator hitValidator,
        SpawnCallback callback) implements Skill {

        private void onCollideWithBlock(ProjectileCollideWithBlockEvent event) {
            ((Mob) event.getEntity()).kill();
        }

        private void onCollideWithEntity(Mob self, ProjectileCollideWithEntityEvent event) {
            if (!hitValidator.valid(self, event.getTarget())) {
                return;
            }

            Mob projectile = (Mob) event.getEntity();

            Entity target = event.getTarget();
            projectile.attack(target);
            self.attack(target);

            projectile.kill();
        }

        private void shootAtPoint(Mob self, Instance instance, Point target) {
            MobSpawner spawner = self.extensions().get(BasicMobSpawner.SPAWNER_KEY);
            spawner.spawn(data.entity, instance, self.getPosition().add(0, self.getEyeHeight(), 0), projectile -> {
                projectile.setOwner(self.getUuid());
                callback.accept(projectile);

                projectile.setNoGravity(!data.gravity);
                projectile.addGoalGroup(new CollectionGoalGroup(List.of(new ProjectileMovementGoal(projectile,
                    projectile, target, data.power(), data.spread(), this::onCollideWithBlock,
                    event -> onCollideWithEntity(self, event)))));
            });
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }

        @Override
        public void use(@NotNull Mob mob) {
            Instance instance = mob.getInstance();
            if (instance == null) {
                return;
            }

            Target target = targetSelector.select(mob);
            Optional<? extends Entity> targetEntityOptional = target.target();
            if (targetEntityOptional.isPresent()) {
                Entity targetEntity = targetEntityOptional.get();
                if (!hitValidator.valid(mob, targetEntity)) {
                    return;
                }

                shootAtPoint(mob, instance, targetEntity.getPosition()
                    .add(0, targetEntity.getBoundingBox().height() / 2, 0));
                return;
            }

            Optional<? extends Point> targetPointOptional = target.location();
            if (targetPointOptional.isEmpty()) {
                return;
            }

            shootAtPoint(mob, instance, targetPointOptional.get());
        }
    }
}
