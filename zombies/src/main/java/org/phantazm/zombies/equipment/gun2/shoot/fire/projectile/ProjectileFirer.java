package org.phantazm.zombies.equipment.gun2.shoot.fire.projectile;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.MobSpawner;
import org.phantazm.mob2.goal.CollectionGoalGroup;
import org.phantazm.mob2.goal.ProjectileMovementGoal;
import org.phantazm.zombies.equipment.gun2.GunModule;
import org.phantazm.zombies.equipment.gun2.Keys;
import org.phantazm.zombies.equipment.gun2.shoot.endpoint.ShotEndpointSelector;
import org.phantazm.zombies.equipment.gun2.shoot.fire.Firer;
import org.phantazm.zombies.player.PlayerComponent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Supplier;

public class ProjectileFirer implements PlayerComponent<Firer> {

    private final Data data;

    private final PlayerComponent<ShotEndpointSelector> endSelector;

    private final PlayerComponent<ProjectileCollisionFilter> collisionFilter;

    public ProjectileFirer(@NotNull Data data, @NotNull PlayerComponent<ShotEndpointSelector> endSelector, @NotNull PlayerComponent<ProjectileCollisionFilter> collisionFilter) {
        this.data = Objects.requireNonNull(data);
        this.endSelector = Objects.requireNonNull(endSelector);
        this.collisionFilter = Objects.requireNonNull(collisionFilter);
    }

    @Override
    public @NotNull Firer forPlayer(@NotNull ZombiesPlayer player, @NotNull InjectionStore injectionStore) {
        GunModule module = injectionStore.get(Keys.GUN_MODULE);
        ShotEndpointSelector endSelectorInstance = endSelector.forPlayer(player, injectionStore);
        ProjectileCollisionFilter collisionFilterInstance = collisionFilter.forPlayer(player, injectionStore);

        return new Impl(module.entitySupplier(), module.mapObjects().mobSpawner(), endSelectorInstance, collisionFilterInstance, data.projectileMob(), data.power(), data.spread(), data.hasGravity());
    }

    private static class Impl implements Firer {

        private final Supplier<Optional<? extends Entity>> entitySupplier;

        private final MobSpawner mobSpawner;

        private final ShotEndpointSelector endSelector;

        private final ProjectileCollisionFilter collisionFilter;

        private final Key projectileMob;

        private final double power;

        private final double spread;

        private final boolean hasGravity;

        public Impl(Supplier<Optional<? extends Entity>> entitySupplier, MobSpawner mobSpawner, ShotEndpointSelector endSelector, ProjectileCollisionFilter collisionFilter, Key projectileMob, double power, double spread, boolean hasGravity) {
            this.entitySupplier = entitySupplier;
            this.mobSpawner = mobSpawner;
            this.endSelector = endSelector;
            this.collisionFilter = collisionFilter;
            this.projectileMob = projectileMob;
            this.power = power;
            this.spread = spread;
            this.hasGravity = hasGravity;
        }

        @Override
        public void fire(@NotNull Pos start, @NotNull Collection<UUID> previousHits) {
            entitySupplier.get().ifPresent(shooter -> {
                Instance instance = shooter.getInstance();
                if (instance == null) {
                    return;
                }

                endSelector.getEnd(start).ifPresent(end -> {
                    if (!mobSpawner.canSpawn(projectileMob)) {
                        return;
                    }

                    Mob mob = mobSpawner.spawn(projectileMob, instance, start, self -> {
                        self.addGoalGroup(new CollectionGoalGroup(Set.of(new ProjectileMovementGoal(self, shooter, end,
                            power, spread, this::onProjectileCollision, this::onProjectileCollision))));
                        self.setNoGravity(!hasGravity);
                    });

                    firedShots.put(mob.getUuid(), new org.phantazm.zombies.equipment.gun.shoot.fire.projectile.ProjectileFirer.FiredShot(gun, state, shooter, start, previousHits));
                    removalQueue.add(new org.phantazm.zombies.equipment.gun.shoot.fire.projectile.ProjectileFirer.AliveProjectile(new WeakReference<>(mob), ticks));
                });
            });
        }

        public void onProjectileCollision(@NotNull ProjectileCollideWithBlockEvent event) {
            org.phantazm.zombies.equipment.gun.shoot.fire.projectile.ProjectileFirer.FiredShot firedShot = firedShots.get(event.getEntity().getUuid());
            if (firedShot == null) {
                return;
            }

            onProjectileCollision(firedShot, event.getEntity(), event.getCollisionPosition());
        }


        public void onProjectileCollision(@NotNull ProjectileCollideWithEntityEvent event) {
            if (!(collisionFilter.shouldExplode(event.getTarget()))) {
                return;
            }

            org.phantazm.zombies.equipment.gun.shoot.fire.projectile.ProjectileFirer.FiredShot firedShot = firedShots.get(event.getEntity().getUuid());
            if (firedShot == null) {
                return;
            }

            onProjectileCollision(firedShot, event.getEntity(), event.getCollisionPosition());
        }
    }

    public record Data(@NotNull Key projectileMob,
        double power,
        double spread,
        boolean hasGravity) {

    }

}
