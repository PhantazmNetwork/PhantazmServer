package org.phantazm.zombies.player;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.BasicTickTaskScheduler;
import org.phantazm.commons.CancellableState;
import org.phantazm.commons.TickTaskScheduler;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.core.inventory.InventoryProfile;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.map.Flaggable;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;
import org.phantazm.zombies.player.state.context.QuitPlayerStateContext;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BasicZombiesPlayer implements ZombiesPlayer, ForwardingAudience {
    private final ZombiesScene scene;
    private final ZombiesPlayerModule module;
    private final TickTaskScheduler scheduler;
    private final Map<UUID, CancellableState> cancellables;

    public BasicZombiesPlayer(@NotNull ZombiesScene scene, @NotNull ZombiesPlayerModule module) {
        this.scene = Objects.requireNonNull(scene, "scene");
        this.module = Objects.requireNonNull(module, "module");
        this.scheduler = new BasicTickTaskScheduler();
        this.cancellables = new ConcurrentHashMap<>();
    }

    @Override
    public @NotNull ZombiesPlayerModule module() {
        return module;
    }

    @Override
    public long getReviveTime() {
        return getPlayer().map(player -> (long)player.getAttributeValue(Attributes.REVIVE_TICKS))
                .orElse((long)Attributes.REVIVE_TICKS.defaultValue());
    }

    @Override
    public @NotNull ZombiesScene getScene() {
        return scene;
    }

    @Override
    public @NotNull TickTaskScheduler scheduler() {
        return scheduler;
    }

    @Override
    public void registerCancellable(@NotNull CancellableState cancellable) {
        CancellableState oldCancellable = cancellables.put(cancellable.id(), cancellable);

        if (oldCancellable != null) {
            oldCancellable.end();
        }

        cancellable.start();
    }

    @Override
    public void removeCancellable(@NotNull UUID id) {
        cancellables.remove(id);
    }

    @Override
    public void start() {
        module.getStateSwitcher().start();

        for (CancellableState cancellableState : cancellables.values()) {
            cancellableState.start();
        }
    }

    @Override
    public void tick(long time) {
        Optional<Player> playerOptional = getPlayer();
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            inventoryTick(player, time);
        }

        module.getStateSwitcher().tick(time);
        scheduler.tick(time);

        for (CancellableState cancellableState : cancellables.values()) {
            cancellableState.end();
        }
    }

    @Override
    public void end() {
        if (!hasQuit()) {
            setState(ZombiesPlayerStateKeys.QUIT, new QuitPlayerStateContext(false));
        }
        module.getStateSwitcher().end();
        scheduler.end();
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
