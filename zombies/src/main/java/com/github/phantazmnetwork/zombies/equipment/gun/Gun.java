package com.github.phantazmnetwork.zombies.equipment.gun;

import com.github.phantazmnetwork.core.inventory.CachedInventoryObject;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.equipment.Upgradable;
import com.github.phantazmnetwork.zombies.equipment.gun.effect.GunEffect;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.Firer;
import com.github.phantazmnetwork.zombies.equipment.gun.visual.GunStackMapper;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Represents an equipment that can shoot.
 * This class mostly maintains state as the definition of shooting is very flexible.
 * The gun's {@link Firer} is responsible for the actual shooting.
 */
public class Gun extends CachedInventoryObject implements Equipment, Upgradable {

    private final Supplier<Optional<? extends Entity>> entitySupplier;

    private final GunModel model;

    private final Set<GunLevel> tickingLevels;

    private Key levelKey;

    private GunLevel level;

    private GunState state;

    private boolean reloadComplete = false;

    /**
     * Creates a new gun.
     *
     * @param entitySupplier A {@link Supplier} that provides the owner of the {@link Gun}
     * @param model          The {@link GunModel} of the {@link Gun}
     */
    public Gun(@NotNull Supplier<Optional<? extends Entity>> entitySupplier, @NotNull GunModel model) {
        this.entitySupplier = Objects.requireNonNull(entitySupplier, "entitySupplier");
        this.model = Objects.requireNonNull(model, "model");
        this.tickingLevels = Collections.newSetFromMap(new IdentityHashMap<>(model.levels().size()));
        this.levelKey = model.rootLevel();
        this.level = model.levels().get(levelKey);
        tickingLevels.add(level);

        GunStats stats = level.stats();
        this.state = new GunState(stats.shootSpeed(), stats.shotInterval(), stats.reloadSpeed(), stats.maxAmmo(),
                                  stats.maxClip(), false, 0
        );
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
     * Makes the gun fire an individual shot.
     * Use {@link #shoot()} to shoot a volley of shots, which should call this internally.
     */
    protected void fire() {
        modifyState(builder -> {
            builder.setTicksSinceLastFire(0L);
            builder.setAmmo(builder.getAmmo() - 1);
            builder.setClip(builder.getClip() - 1);
        });
        if (state.clip() == 0) {
            if (state.ammo() > 0) {
                reload();
            }
            else {
                for (GunEffect effect : level.noAmmoEffects()) {
                    effect.apply(state);
                }
            }
        }

        entitySupplier.get().ifPresent(entity -> {
            Pos start = entity.getPosition().add(0, entity.getEyeHeight(), 0);
            level.firer().fire(state, start, new HashSet<>());
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
            modifyState(builder -> builder.setTicksSinceLastReload(0L));
            reloadComplete = false;
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
        ItemStack stack = level.stack();
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
            }
            else if (!reloadComplete) {
                builder.setClip(Math.min(level.stats().maxClip(), getState().ammo()));
                reloadComplete = true;
            }
        });

        if (state.queuedShots() > 0) {
            if (level.shootTester().canFire(state)) {
                fire();
                modifyState(builder -> builder.setQueuedShots(state.queuedShots() - 1));
            }
            else if (!level.shootTester().isFiring(state)) {
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

    @Override
    public void setLevel(@NotNull Key key) {
        Objects.requireNonNull(key, "key");
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
    }
}
