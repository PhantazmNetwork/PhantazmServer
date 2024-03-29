package org.phantazm.zombies.player;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.tick.TickTaskScheduler;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.core.inventory.InventoryProfile;
import org.phantazm.zombies.Attributes;
import org.phantazm.commons.flag.Flaggable;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;
import org.phantazm.zombies.player.state.context.QuitPlayerStateContext;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class BasicZombiesPlayer implements ZombiesPlayer, ForwardingAudience {
    private final ZombiesScene scene;
    private final ZombiesPlayerModule module;
    private final TickTaskScheduler taskScheduler;

    private final AtomicBoolean blockHandAnimation;

    public BasicZombiesPlayer(@NotNull ZombiesScene scene, @NotNull ZombiesPlayerModule module,
        @NotNull TickTaskScheduler taskScheduler) {
        this.scene = Objects.requireNonNull(scene);
        this.module = Objects.requireNonNull(module);
        this.taskScheduler = Objects.requireNonNull(taskScheduler);
        this.blockHandAnimation = new AtomicBoolean();
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
    public void start() {
        module.getStateSwitcher().start();
    }

    @Override
    public void tick(long time) {
        Optional<Player> playerOptional = getPlayer();
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            inventoryTick(player, time);
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
