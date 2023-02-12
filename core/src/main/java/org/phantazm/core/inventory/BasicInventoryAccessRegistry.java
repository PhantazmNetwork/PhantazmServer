package org.phantazm.core.inventory;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.core.equipment.Equipment;
import org.phantazm.core.player.PlayerView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Basic implementation of an {@link InventoryAccessRegistry}.
 */
public class BasicInventoryAccessRegistry implements InventoryAccessRegistry {
    private final Map<Key, InventoryAccess> accessMap = new HashMap<>();
    private final Object sync = new Object();
    private InventoryAccess currentAccess = null;

    @Override
    public @NotNull Optional<InventoryAccess> getCurrentAccess() {
        return Optional.ofNullable(currentAccess);
    }

    @Override
    public void switchAccess(@Nullable Key key, @NotNull PlayerView playerView) {
        synchronized (sync) {
            if (key == null) {
                currentAccess = null;
            }
            else {
                InventoryAccess access = accessMap.get(key);
                if (access == null) {
                    throw new IllegalArgumentException("No matching inventory access found");
                }

                currentAccess = access;
                applyTo(access, playerView);
            }
        }
    }

    @Override
    public void registerAccess(@NotNull Key key, @NotNull InventoryAccess profile) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(profile, "profile");

        synchronized (sync) {
            if (accessMap.containsKey(key)) {
                throw new IllegalArgumentException("Inventory profile already registered");
            }

            accessMap.put(key, profile);
        }
    }

    @Override
    public void unregisterAccess(@NotNull Key key) {
        Objects.requireNonNull(key, "key");

        synchronized (sync) {
            if (!accessMap.containsKey(key)) {
                throw new IllegalArgumentException("Inventory profile not yet registered");
            }

            accessMap.remove(key);
        }
    }

    private void applyTo(InventoryAccess currentAccess, PlayerView playerView) {
        playerView.getPlayer().ifPresent(player -> {
            player.getInventory().clear();

            InventoryProfile profile = currentAccess.profile();

            for (int slot = 0; slot < profile.getSlotCount(); slot++) {
                if (!profile.hasInventoryObject(slot)) {
                    continue;
                }

                InventoryObject inventoryObject = profile.getInventoryObject(slot);
                if (inventoryObject.shouldRedraw()) {
                    player.getInventory().setItemStack(slot, inventoryObject.getItemStack());
                }

                if (slot == player.getHeldSlot() && inventoryObject instanceof Equipment equipment) {
                    equipment.setSelected(true);
                }
            }
        });
    }

}
