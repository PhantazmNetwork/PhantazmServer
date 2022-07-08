package com.github.phantazmnetwork.zombies.equipment.gun;

import com.github.phantazmnetwork.api.inventory.CachedInventoryObject;
import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.equipment.Upgradable;
import com.github.phantazmnetwork.zombies.equipment.gun.effect.GunEffect;
import com.github.phantazmnetwork.zombies.equipment.gun.visual.GunStackMapper;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class Gun extends CachedInventoryObject implements Equipment, Upgradable {

    private final PlayerView playerView;

    private final GunModel model;

    private final Set<GunLevel> tickingLevels;

    private GunState state;

    private Key levelKey;

    private GunLevel level;

    private boolean reloadComplete = false;

    public Gun(@NotNull PlayerView playerView, @NotNull GunModel model) {
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.model = Objects.requireNonNull(model, "model");
        this.tickingLevels = Collections.newSetFromMap(new IdentityHashMap<>(model.levels().size()));

        GunStats stats = getLevel().stats();
        this.state = new GunState(stats.shootSpeed(), stats.shotInterval(), stats.reloadSpeed(), stats.maxAmmo(),
                stats.maxClip(), false, 0);
        this.levelKey = model.rootLevel();
        this.level = model.levels().get(levelKey);
        tickingLevels.add(level);
    }

    public void shoot() {
        if (getLevel().shootTester().shouldShoot(state)) {
            modifyState(state -> {
                state.setQueuedShots(getLevel().stats().shots() - 1);
                state.setTicksSinceLastShot(0L);
            });
            internalShoot();
        }
    }

    protected void internalShoot() {
        modifyState(builder -> {
            builder.setTicksSinceLastFire(0L);
            builder.setAmmo(builder.getAmmo() - 1);
            builder.setClip(builder.getClip() - 1);
        });
        if (state.clip() == 0) {
            if (state.ammo() > 0) {
                reload();
            } else {
                for (GunEffect effect : getLevel().noAmmoEffects()) {
                    effect.apply(state);
                }
            }
        }

        playerView.getPlayer().ifPresent(player -> {
            Pos start = player.getPosition().add(0, player.getEyeHeight(), 0);
            getLevel().firer().fire(state, start, new HashSet<>());
        });
        for (GunEffect effect : getLevel().shootEffects()) {
            effect.apply(state);
        }
    }

    public void reload() {
        if (getLevel().reloadTester().shouldReload(state)) {
            modifyState(builder -> builder.setTicksSinceLastReload(0L));
            reloadComplete = false;
            for (GunEffect reloadEffect : getLevel().reloadEffects()) {
                reloadEffect.apply(state);
            }
        }
    }

    public void refill() {
        modifyState(builder -> {
            builder.setAmmo(getLevel().stats().maxAmmo());
            builder.setClip(getLevel().stats().maxClip());
            builder.setTicksSinceLastReload(getLevel().stats().reloadSpeed());
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

    public @NotNull GunState getState() {
        return state;
    }

    public @NotNull GunLevel getLevel() {
        return level;
    }

    private void modifyState(@NotNull Consumer<GunState.Builder> consumer) {
        GunState.Builder builder = state.toBuilder();
        consumer.accept(builder);
        state = builder.build();
    }

    @Override
    protected @NotNull ItemStack computeStack() {
        ItemStack stack = getLevel().stack();
        for (GunStackMapper mapper : getLevel().gunStackMappers()) {
            stack = mapper.map(state, stack);
        }

        return stack;
    }

    @Override
    public void tick(long time) {
        modifyState(builder -> {
            if (getLevel().shootTester().isShooting(state)) {
                builder.setTicksSinceLastShot(builder.getTicksSinceLastShot() + 1);
            }
            if (getLevel().shootTester().isFiring(state)) {
                builder.setTicksSinceLastFire(builder.getTicksSinceLastFire() + 1);
            }
            if (getLevel().reloadTester().isReloading(state)) {
                builder.setTicksSinceLastReload(builder.getTicksSinceLastReload() + 1);
            }
            else if (!reloadComplete) {
                builder.setClip(Math.min(getLevel().stats().maxClip(), getState().ammo()));
                reloadComplete = true;
            }
        });

        if (state.queuedShots() > 0) {
            if (getLevel().shootTester().canFire(state)) {
                internalShoot();
                modifyState(builder -> builder.setQueuedShots(state.queuedShots() - 1));
            }
            else if (!getLevel().shootTester().isFiring(state)) {
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
    public @NotNull Collection<Key> getLevels() {
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
        for (GunEffect effect : level.startEffects()) {
            effect.apply(state);
        }
    }
}
