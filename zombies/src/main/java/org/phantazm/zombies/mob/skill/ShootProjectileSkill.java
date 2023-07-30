package org.phantazm.zombies.mob.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.MobModel;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.goal.CollectionGoalGroup;
import org.phantazm.mob.goal.ProjectileMovementGoal;
import org.phantazm.mob.skill.Skill;
import org.phantazm.mob.target.TargetSelector;
import org.phantazm.mob.validator.TargetValidator;
import org.phantazm.proxima.bindings.minestom.ProximaEntity;
import org.phantazm.zombies.Tags;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.mob.skill.hit_action.ProjectileHitEntityAction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Model("zombies.mob.skill.shoot_projectile")
@Cache(false)
public class ShootProjectileSkill implements Skill {
    private final Data data;
    private final MapObjects mapObjects;
    private final TargetSelector<? extends Entity> targetSelector;
    private final TargetValidator targetValidator;
    private final TargetValidator hitValidator;
    private final List<ProjectileHitEntityAction> actions;

    private final UUID uuid;

    @FactoryMethod
    public ShootProjectileSkill(@NotNull Data data, @NotNull MapObjects mapObjects,
            @NotNull @Child("target_selector") TargetSelector<? extends Entity> targetSelector,
            @NotNull @Child("target_validator") TargetValidator targetValidator,
            @NotNull @Child("hit_validator") TargetValidator hitValidator,
            @NotNull @Child("actions") List<ProjectileHitEntityAction> actions) {
        this.data = data;
        this.mapObjects = mapObjects;
        this.targetSelector = targetSelector;
        this.targetValidator = targetValidator;
        this.hitValidator = hitValidator;
        this.actions = actions;

        this.uuid = UUID.randomUUID();

        EventNode<Event> eventNode = mapObjects.module().eventNode().get();

        eventNode.addListener(ProjectileCollideWithEntityEvent.class, this::onCollideWithEntity);
        eventNode.addListener(ProjectileCollideWithBlockEvent.class, this::onCollideWithBlock);
    }

    private void onCollideWithBlock(ProjectileCollideWithBlockEvent event) {
        Entity projectile = event.getEntity();

        UUID identifier = projectile.getTag(Tags.SKILL_IDENTIFIER);
        if (identifier == null || !identifier.equals(uuid)) {
            return;
        }

        killOrRemove(projectile);
    }

    private void onCollideWithEntity(ProjectileCollideWithEntityEvent event) {
        Entity projectile = event.getEntity();

        UUID shooter = projectile.getTag(Tags.PROJECTILE_SHOOTER);
        if (shooter == null) {
            return;
        }

        UUID identifier = projectile.getTag(Tags.SKILL_IDENTIFIER);
        if (identifier == null || !identifier.equals(uuid)) {
            return;
        }

        PhantazmMob shootingMob = mapObjects.module().mobStore().getMob(shooter);
        if (!hitValidator.valid(shootingMob == null ? null : shootingMob.entity(), event.getTarget())) {
            return;
        }

        for (ProjectileHitEntityAction action : actions) {
            action.perform(shootingMob, projectile, event.getTarget());
        }

        killOrRemove(projectile);
    }

    private void killOrRemove(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.kill();
        }
        else {
            entity.remove();
        }
    }

    @Override
    public void use(@NotNull PhantazmMob self) {
        Instance instance = self.entity().getInstance();
        if (instance == null) {
            return;
        }

        Optional<? extends Entity> targetOptional = targetSelector.selectTarget(self);
        if (targetOptional.isEmpty()) {
            return;
        }

        MobModel model = mapObjects.module().mobModelFunction().apply(data.entity);
        if (model == null) {
            return;
        }

        ProximaEntity selfEntity = self.entity();
        Entity targetEntity = targetOptional.get();
        if (!targetValidator.valid(selfEntity, targetEntity)) {
            return;
        }

        Pos selfPosition = selfEntity.getPosition();
        PhantazmMob mob = mapObjects.mobSpawner()
                .spawn(mapObjects.module().instance(), selfPosition.add(0, selfEntity.getEyeHeight(), 0), model);
        if (data.addToRound) {
            mapObjects.module().roundHandlerSupplier().get().currentRound().ifPresent(round -> {
                round.addMob(mob);
            });
        }

        ProximaEntity mobEntity = mob.entity();

        mobEntity.setTag(Tags.PROJECTILE_SHOOTER, selfEntity.getUuid());
        mobEntity.setTag(Tags.SKILL_IDENTIFIER, uuid);

        mobEntity.setNoGravity(!data.gravity);
        mobEntity.addGoalGroup(new CollectionGoalGroup(List.of(new ProjectileMovementGoal(mob.entity(), selfEntity,
                targetEntity.getPosition().add(0, targetEntity.getBoundingBox().height() / 2, 0), data.power(),
                data.spread()))));
    }

    @DataObject
    public record Data(@NotNull Key entity,
                       double power,
                       double spread,
                       boolean gravity,
                       boolean addToRound,
                       @NotNull @ChildPath("target_selector") String targetSelector,
                       @NotNull @ChildPath("target_validator") String targetValidator,
                       @NotNull @ChildPath("hit_validator") String hitValidator,
                       @NotNull @ChildPath("actions") List<String> hitEntityActions) {
        @Default("spread")
        public static @NotNull ConfigElement defaultSpread() {
            return ConfigPrimitive.of(0.0D);
        }

        @Default("gravity")
        public static @NotNull ConfigElement defaultGravity() {
            return ConfigPrimitive.of(true);
        }

        @Default("addToRound")
        public static @NotNull ConfigElement defaultAddToRound() {
            return ConfigPrimitive.of(false);
        }
    }
}
