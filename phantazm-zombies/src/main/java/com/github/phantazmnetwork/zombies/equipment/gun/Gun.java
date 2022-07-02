package com.github.phantazmnetwork.zombies.equipment.gun;

import com.github.phantazmnetwork.api.inventory.CachedInventoryObject;
import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.api.target.TargetSelectorInstance;
import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.equipment.Upgradable;
import com.github.phantazmnetwork.zombies.equipment.gun.effect.GunEffect;
import com.github.phantazmnetwork.zombies.equipment.gun.shot.GunShot;
import com.github.phantazmnetwork.zombies.equipment.gun.shot.handler.ShotHandler;
import com.github.phantazmnetwork.zombies.equipment.gun.state.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.visual.GunStackMapper;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

public class Gun extends CachedInventoryObject implements Equipment, Keyed, Upgradable {

    private final PlayerView playerView;

    private final GunModel model;

    private final MobStore store;

    private TargetSelectorInstance<? extends GunShot> selector;

    private GunState state;

    private int level;

    private boolean reloadComplete = false;

    public Gun(@NotNull PlayerView playerView, @NotNull GunModel model, @NotNull MobStore store) {
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.model = Objects.requireNonNull(model, "model");
        this.store = Objects.requireNonNull(store, "store");
        this.level = 0;

        GunLevel level = getLevel();
        this.selector = level.selector().createSelector(store, playerView);
        this.state = new GunState(level.shootSpeed(), level.reloadSpeed(), level.maxAmmo(), level.maxClip(),
                false, 0);
    }

    public void shoot() {
        if (shouldShoot()) {
            modifyState(state -> state.setQueuedShots(getLevel().shots() - 1));
            internalShoot();
        }
    }

    protected void internalShoot() {
        modifyState(builder -> {
            builder.setTicksSinceLastShot(0L);
            builder.setAmmo(builder.getAmmo() - 1);
            builder.setClip(builder.getClip() - 1);
        });
        if (state.clip() == 0) {
            if (state.ammo() > 0) {
                reload();
            } else {
                for (GunEffect effect : getLevel().emptyClipEffects()) {
                    effect.accept(this);
                }
            }
        }
        playerView.getPlayer().ifPresent(player -> {
            selector.selectTarget().ifPresent(hit -> {
                processHit(player, hit);
            });
        });

        for (GunEffect effect : getLevel().shootEffects()) {
            effect.accept(this);
        }
    }

    protected boolean shouldShoot() {
        return !isShooting() && canFire() && state.queuedShots() == 0;
    }

    protected boolean canFire() {
        return state.ammo() > 0 && canReload();
    }

    public boolean isShooting() {
        return state.ticksSinceLastShot() < getLevel().shootSpeed();
    }

    protected void processHit(@NotNull Player player, @NotNull GunShot hit) {
        for (ShotHandler handler : getLevel().shotHandlers()) {
            handler.handle(player, hit);
        }
    }

    public void reload() {
        if (shouldReload()) {
            modifyState(builder -> builder.setTicksSinceLastReload(0L));
            reloadComplete = false;
            for (GunEffect reloadEffect : getLevel().reloadEffects()) {
                reloadEffect.accept(this);
            }
        }
    }

    protected boolean shouldReload() {
        return canReload() && state.clip() != getLevel().maxClip();
    }

    protected boolean canReload() {
        return !isReloading() && state.ammo() > 0;
    }

    public boolean isReloading() {
        return state.ticksSinceLastReload() < getLevel().reloadSpeed();
    }

    public void refill() {
        modifyState(builder -> {
            builder.setAmmo(getLevel().maxAmmo());
            builder.setClip(getLevel().maxClip());
            builder.setTicksSinceLastReload(getLevel().reloadSpeed());
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

    public @NotNull PlayerView getOwner() {
        return playerView;
    }

    public @NotNull GunState getState() {
        return state;
    }

    public @NotNull GunLevel getLevel() {
        return model.getLevels().get(level);
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
            stack = mapper.map(this, stack);
        }

        return stack;
    }

    @Override
    public void tick(long time) {
        modifyState(builder -> {
            if (builder.getTicksSinceLastShot() < getLevel().shootSpeed()) {
                builder.setTicksSinceLastShot(builder.getTicksSinceLastShot() + 1);
            }
            if (builder.getTicksSinceLastReload() < getLevel().reloadSpeed()) {
                builder.setTicksSinceLastReload(builder.getTicksSinceLastReload() + 1);
            }
            else if (!reloadComplete && builder.getTicksSinceLastReload() == getLevel().reloadSpeed()) {
                builder.setClip(Math.min(getLevel().maxClip(), getState().ammo()));
                reloadComplete = true;
            }
        });

        if (state.queuedShots() > 0) {
            int postQueuedShots;
            if (canFire()) {
                internalShoot();
                postQueuedShots = state.queuedShots() - 1;
            }
            else {
                postQueuedShots = 0;
            }

            modifyState(builder -> builder.setQueuedShots(postQueuedShots));
        }

        for (GunEffect effect : getLevel().tickEffects()) {
            effect.accept(this);
        }

        setDirty();
    }

    @Override
    public @NotNull Key key() {
        return model.key();
    }

    @Override
    public void upgrade() {
        if (level < model.getLevels().size() - 1) {
            level++;
            selector = getLevel().selector().createSelector(store, playerView);
        }

        setDirty();
    }

}
