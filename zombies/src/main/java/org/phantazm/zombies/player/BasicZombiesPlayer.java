package org.phantazm.zombies.player;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.inventory.InventoryAccessRegistry;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.core.inventory.InventoryProfile;
import org.phantazm.zombies.equipment.Equipment;
import org.phantazm.zombies.map.Flaggable;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("ClassCanBeRecord")
public class BasicZombiesPlayer implements ZombiesPlayer {

    private final ZombiesPlayerModule module;

    public BasicZombiesPlayer(@NotNull ZombiesPlayerModule module) {
        this.module = Objects.requireNonNull(module, "module");
    }

    @Override
    public @NotNull ZombiesPlayerModule getModule() {
        return module;
    }

    @Override
    public long getReviveTime() {
        return 30L;// todo: fast revive
    }

    @Override
    public void start() {
        module.getStateSwitcher().start();
        getPlayer().ifPresent(player -> {
            InventoryAccessRegistry accessRegistry = module.getInventoryAccessRegistry();
            if (accessRegistry.hasCurrentAccess()) {
                InventoryProfile profile = accessRegistry.getCurrentAccess().profile();
                for (int slot = 0; slot < profile.getSlotCount(); ++slot) {
                    if (profile.hasInventoryObject(slot)) {
                        InventoryObject inventoryObject = profile.getInventoryObject(slot);
                        if (inventoryObject.shouldRedraw()) {
                            player.getInventory().setItemStack(slot, inventoryObject.getItemStack());
                        }
                        if (slot == player.getHeldSlot() && inventoryObject instanceof Equipment equipment) {
                            equipment.setSelected(true);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void tick(long time) {
        Optional<Player> playerOptional = getPlayer();
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            module.getMeta().setCrouching(player.getPose() == Entity.Pose.SNEAKING);

            inventoryTick(player, time);
        }
        else {
            module.getMeta().setCrouching(false);
        }

        module.getStateSwitcher().tick(time);
    }

    @Override
    public void end() {
        if (!isState(ZombiesPlayerStateKeys.QUIT)) {
            getPlayer().ifPresent(player -> player.getInventory().clear());
        }
        module.getStateSwitcher().end();
    }

    private void inventoryTick(Player player, long time) {
        InventoryAccessRegistry accessRegistry = module.getInventoryAccessRegistry();
        if (accessRegistry.hasCurrentAccess()) {
            InventoryProfile profile = accessRegistry.getCurrentAccess().profile();
            for (int slot = 0; slot < profile.getSlotCount(); ++slot) {
                if (profile.hasInventoryObject(slot)) {
                    InventoryObject inventoryObject = profile.getInventoryObject(slot);
                    inventoryObject.tick(time);

                    if (inventoryObject.shouldRedraw()) {
                        player.getInventory().setItemStack(slot, inventoryObject.getItemStack());
                    }
                }
            }
        }
    }

    @Override
    public @NotNull Flaggable flags() {
        return module.flaggable();
    }
}
