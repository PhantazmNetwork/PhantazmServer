package org.phantazm.zombies.player;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.flag.Flaggable;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.core.inventory.InventoryProfile;
import org.phantazm.core.tick.Activable;
import org.phantazm.core.tick.TickTaskScheduler;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;
import org.phantazm.zombies.player.state.context.QuitPlayerStateContext;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BasicZombiesPlayer implements ZombiesPlayer, ForwardingAudience {
    private final ZombiesScene scene;
    private final ZombiesPlayerModule module;
    private final TickTaskScheduler taskScheduler;

    private final AtomicBoolean blockHandAnimation;
    private volatile Set<Activable> activables;

    private final Lock activablesLock;

    public BasicZombiesPlayer(@NotNull ZombiesScene scene, @NotNull ZombiesPlayerModule module,
        @NotNull TickTaskScheduler taskScheduler) {
        this.scene = Objects.requireNonNull(scene);
        this.module = Objects.requireNonNull(module);
        this.taskScheduler = Objects.requireNonNull(taskScheduler);
        this.blockHandAnimation = new AtomicBoolean();
        this.activables = Set.of();
        this.activablesLock = new ReentrantLock();
    }

    @Override
    public @NotNull ZombiesPlayerModule module() {
        return module;
    }

    @Override
    public long getReviveTime() {
        return getPlayer().map(player -> (long) player.getAttributeValue(Attributes.REVIVE_TICKS))
            .orElse((long) Attributes.REVIVE_TICKS.defaultValue());
    }

    @Override
    public @NotNull ZombiesScene getScene() {
        return scene;
    }

    @Override
    public void setBlockHandAnimation() {
        blockHandAnimation.set(true);
    }

    @Override
    public boolean blockHandAnimation() {
        return blockHandAnimation.getAndSet(false);
    }

    @Override
    public void addActivable(@NotNull Activable activable) {
        activablesLock.lock();
        try {
            Set<Activable> mutableActivables = new HashSet<>(activables);
            mutableActivables.add(activable);

            this.activables = mutableActivables;
        } finally {
            activablesLock.unlock();
        }

        activable.start();
    }

    @Override
    public void removeActivable(@NotNull Activable activable) {
        boolean removed;
        activablesLock.lock();
        try {
            Set<Activable> mutableActivables = new HashSet<>(activables);
            removed = mutableActivables.remove(activable);

            this.activables = mutableActivables;
        } finally {
            activablesLock.unlock();
        }

        if (removed) {
            activable.end();
        }
    }

    @Override
    public void start() {
        module.getStateSwitcher().start();

        for (Activable activable : activables) {
            activable.start();
        }
    }

    @Override
    public void tick(long time) {
        Optional<Player> playerOptional = getPlayer();
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            inventoryTick(player, time);
        }

        for (Activable activable : activables) {
            activable.tick(time);
        }

        module.getStateSwitcher().tick(time);
        taskScheduler.tick(time);
        module.getCoins().tick(time);
        module.getActionBar().tick(time);
    }

    @Override
    public void end() {
        if (!hasQuit()) {
            setState(ZombiesPlayerStateKeys.QUIT, new QuitPlayerStateContext(false));
        }
        module.getStateSwitcher().end();

        for (Activable activable : activables) {
            activable.end();
        }
    }

    private void inventoryTick(Player player, long time) {
        module.getInventoryAccessRegistry().getCurrentAccess().ifPresent(inventoryAccess -> {
            InventoryProfile profile = inventoryAccess.profile();
            for (int slot = 0; slot < profile.getSlotCount(); ++slot) {
                if (profile.hasInventoryObject(slot)) {
                    InventoryObject inventoryObject = profile.getInventoryObject(slot);
                    inventoryObject.tick(time);

                    if (inventoryObject.shouldRedraw()) {
                        player.getInventory().setItemStack(slot, inventoryObject.getItemStack());
                    }
                }
            }
        });
    }

    @Override
    public @NotNull Flaggable flags() {
        return module.flags();
    }

    @Override
    public @NotNull Iterable<? extends Audience> audiences() {
        Optional<Player> playerOptional = getPlayer();
        if (playerOptional.isEmpty()) {
            return List.of();
        }

        return List.of(playerOptional.get());
    }
}
