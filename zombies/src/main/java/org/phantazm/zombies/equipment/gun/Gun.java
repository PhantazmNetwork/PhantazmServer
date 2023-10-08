package org.phantazm.zombies.equipment.gun;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.Equipment;
import org.phantazm.core.equipment.Upgradable;
import org.phantazm.core.inventory.CachedInventoryObject;
import org.phantazm.zombies.equipment.gun.effect.GunEffect;
import org.phantazm.zombies.equipment.gun.shoot.fire.Firer;
import org.phantazm.zombies.equipment.gun.visual.GunStackMapper;
import org.phantazm.zombies.event.equipment.GunLoseAmmoEvent;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Represents an equipment that can shoot. This class mostly maintains state as the definition of shooting is very
 * flexible. The gun's {@link Firer} is responsible for the actual shooting.
 */
public class Gun extends CachedInventoryObject implements Equipment, Upgradable {
    private final Key equipmentKey;
    private final Supplier<Optional<? extends Entity>> entitySupplier;
    private final GunModel model;
    private final Set<GunLevel> tickingLevels;
    private Key levelKey;
    private GunLevel level;
    private GunState state;

    /**
     * Creates a new gun.
     *
     * @param entitySupplier A {@link Supplier} that provides the owner of the {@link Gun}
     * @param model          The {@link GunModel} of the {@link Gun}
     */
    public Gun(@NotNull Key equipmentKey, @NotNull Supplier<Optional<? extends Entity>> entitySupplier,
        @NotNull GunModel model) {
        this.equipmentKey = Objects.requireNonNull(equipmentKey);
        this.entitySupplier = Objects.requireNonNull(entitySupplier);
        this.model = Objects.requireNonNull(model);
        this.tickingLevels = Collections.newSetFromMap(new IdentityHashMap<>(model.levels().size()));
        this.levelKey = model.rootLevel();
        this.level = model.levels().get(levelKey);
        tickingLevels.add(level);

        GunStats stats = level.stats();
        this.state = new GunState(stats.shootSpeed(), stats.shotInterval(), stats.reloadSpeed(), false, stats.maxAmmo(),
            stats.maxClip(), false, 0);
    }

    /**
     * Shoots the gun. A gun may fire multiple times in one shot.
     */
    public void shoot() {
        if (level.shootTester().shouldShoot(state)) {
            modifyState(state -> {
                state.setQueuedShots(level.stats().shots() - 1);
                state.setTicksSinceLastShot(0L);
            });
            fire();
        }
    }

    /**
     * Makes the gun fire an individual shot. Use {@link #shoot()} to shoot a volley of shots, which should call this
     * internally.
     */
    protected void fire() {
        Optional<? extends Entity> entityOptional = entitySupplier.get();

        int ammoLoss;
        if (entityOptional.isPresent()) {
            GunLoseAmmoEvent event = new GunLoseAmmoEvent(entityOptional.get(), this, 1);
            EventDispatcher.call(event);
            ammoLoss = event.getAmmoLost();
        } else {
            ammoLoss = 1;
        }

        modifyState(builder -> {
            builder.setTicksSinceLastFire(0L);
            builder.setAmmo(builder.getAmmo() - ammoLoss);
            builder.setClip(builder.getClip() - ammoLoss);
        });

        if (state.clip() == 0) {
            if (state.ammo() > 0) {
                reload();
            } else {
                for (GunEffect effect : level.noAmmoEffects()) {
                    effect.apply(state);
                }
            }
        }

        entitySupplier.get().ifPresent(entity -> {
            Pos start = entity.getPosition().add(0, entity.getEyeHeight(), 0);
            level.firer().fire(this, state, start, new HashSet<>());
        });
        for (GunEffect effect : level.shootEffects()) {
            effect.apply(state);
        }
    }

    /**
     * Reloads the gun.
     */
    public void reload() {
        if (level.reloadTester().shouldReload(state)) {
            modifyState(builder -> {
                builder.setTicksSinceLastReload(0L);
                builder.setReloadComplete(false);
            });
            for (GunEffect reloadEffect : level.reloadEffects()) {
                reloadEffect.apply(state);
            }
        }
    }

    /**
     * Refills the gun's ammo and clip.
     */
    public void refill() {
        modifyState(builder -> {
            builder.setAmmo(level.stats().maxAmmo());
            builder.setClip(level.stats().maxClip());
            builder.setTicksSinceLastReload(level.stats().reloadSpeed());
        });
    }

    @Override
    public void setSelected(boolean selected) {
        modifyState(builder -> builder.setMainEquipment(selected));
    }

    @Override
    public void rightClick() {
        shoot();
    }

    @Override
    public void leftClick() {
        reload();
    }

    @Override
    public void attack(@NotNull Entity target) {

    }

    /**
     * Gets the current {@link GunState} of the {@link Gun}
     *
     * @return The current {@link GunState} of the {@link Gun}
     */
    public @NotNull GunState getState() {
        return state;
    }

    private void modifyState(@NotNull Consumer<GunState.Builder> consumer) {
        GunState.Builder builder = state.toBuilder();
        consumer.accept(builder);
        state = builder.build();
    }

    @Override
    protected @NotNull ItemStack computeStack() {
        ItemStack stack = level.data().stack();
        for (GunStackMapper mapper : level.gunStackMappers()) {
            stack = mapper.map(state, stack);
        }

        return stack;
    }

    @Override
    public void tick(long time) {
        modifyState(builder -> {
            if (level.shootTester().isShooting(state)) {
                builder.setTicksSinceLastShot(builder.getTicksSinceLastShot() + 1);
            }
            if (level.shootTester().isFiring(state)) {
                builder.setTicksSinceLastFire(builder.getTicksSinceLastFire() + 1);
            }
            if (level.reloadTester().isReloading(state)) {
                builder.setTicksSinceLastReload(builder.getTicksSinceLastReload() + 1);
            } else if (!builder.isReloadComplete()) {
                builder.setClip(Math.min(level.stats().maxClip(), getState().ammo()));
                builder.setReloadComplete(true);
            }
        });

        if (state.queuedShots() > 0) {
            if (level.shootTester().canFire(state)) {
                fire();
                modifyState(builder -> builder.setQueuedShots(state.queuedShots() - 1));
            } else if (!level.shootTester().isFiring(state)) {
                modifyState(builder -> builder.setQueuedShots(0));
            }

        }

        for (GunLevel tickingLevel : tickingLevels) {
            tickingLevel.firer().tick(state, time);
            for (GunEffect effect : tickingLevel.shootEffects()) {
                effect.tick(state, time);
            }
            for (GunEffect effect : tickingLevel.reloadEffects()) {
                effect.tick(state, time);
            }
            for (GunEffect effect : tickingLevel.noAmmoEffects()) {
                effect.tick(state, time);
            }
        }
        for (GunEffect effect : level.tickEffects()) {
            effect.apply(state);
        }

        setDirty();
    }

    @Override
    public @NotNull Set<Key> getSuggestedUpgrades() {
        return level.upgrades();
    }

    @Override
    public @NotNull Set<Key> getLevels() {
        return model.levels().keySet();
    }

    public @NotNull GunLevel getLevel() {
        return level;
    }

    @Override
    public void setLevel(@NotNull Key key) {
        Objects.requireNonNull(key);
        if (levelKey.equals(key)) {
            return;
        }

        GunLevel newLevel = model.levels().get(key);
        if (newLevel == null) {
            throw new IllegalArgumentException("No level with key " + key);
        }

        levelKey = key;
        level = newLevel;
        tickingLevels.add(newLevel);

        modifyState(builder -> {
            builder.setAmmo(newLevel.stats().maxAmmo());
            builder.setClip(newLevel.stats().maxClip());
            builder.setTicksSinceLastReload(newLevel.stats().reloadSpeed());
            builder.setTicksSinceLastShot(newLevel.stats().shootSpeed());
            builder.setTicksSinceLastFire(newLevel.stats().shotInterval());
        });
        for (GunEffect effect : level.activateEffects()) {
            effect.apply(state);
        }

        setDirty();
    }

    @Override
    public @NotNull Key currentLevel() {
        return levelKey;
    }

    @Override
    public @NotNull Key key() {
        return equipmentKey;
    }
}
