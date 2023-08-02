package org.phantazm.core.inventory;

import net.kyori.adventure.key.Key;
import net.minestom.server.item.ItemStack;
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
    private volatile InventoryAccess currentAccess = null;
    private final PlayerView playerView;

    public BasicInventoryAccessRegistry(@NotNull PlayerView playerView) {
        this.playerView = Objects.requireNonNull(playerView, "playerView");
    }

    @Override
    public @NotNull Optional<InventoryAccess> getCurrentAccess() {
        return Optional.ofNullable(currentAccess);
    }

    @Override
    public void switchAccess(@Nullable Key key) {
        synchronized (sync) {
            InventoryAccess oldAccess = this.currentAccess;

            if (key == null) {
                if (oldAccess == null) {
                    return;
                }

                this.currentAccess = null;
                applyTo(null, oldAccess);
                return;
            }

            InventoryAccess newAccess = accessMap.get(key);
            if (newAccess == null) {
                throw new IllegalArgumentException("No matching inventory access found");
            }

            if (newAccess == currentAccess) {
                return;
            }

            this.currentAccess = newAccess;
            applyTo(newAccess, oldAccess);
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
    public boolean canPushTo(@NotNull InventoryAccess currentAccess, @NotNull Key groupKey) {
        InventoryObjectGroup group = currentAccess.groups().get(groupKey);
        return group != null && !group.isFull();
    }

    @Override
    public @Nullable InventoryObject replaceObject(@NotNull InventoryAccess access, int slot,
            @NotNull InventoryObject newObject) {
        InventoryProfile profile = access.profile();
        InventoryObject old = null;
        if (profile.hasInventoryObject(slot)) {
            old = profile.removeInventoryObject(slot);

            if (access == currentAccess) {
                old.end();
            }
        }

        profile.setInventoryObject(slot, newObject);

        if (access == currentAccess) {
            onAdd(slot, newObject);
        }

        return old;
    }

    @Override
    public @Nullable InventoryObject removeObject(@NotNull InventoryAccess access, int slot) {
        InventoryProfile profile = access.profile();

        InventoryObject old = null;
        if (profile.hasInventoryObject(slot)) {
            old = profile.removeInventoryObject(slot);

            if (access == this.currentAccess) {
                old.end();
                playerView.getPlayer().ifPresent(player -> player.getInventory().setItemStack(slot, ItemStack.AIR));
            }
        }

        return old;
    }

    @Override
    public void pushObject(@NotNull InventoryAccess access, @NotNull Key groupKey, @NotNull InventoryObject object) {
        InventoryObjectGroup group = getGroup(access, groupKey);

        int slot = group.pushInventoryObject(object);
        if (access == currentAccess) {
            onAdd(slot, object);
        }
    }

    @Override
    public @NotNull InventoryAccess getAccess(@NotNull Key profileKey) {
        synchronized (sync) {
            return Objects.requireNonNull(accessMap.get(profileKey), "no access named " + profileKey);
        }
    }

    private void onAdd(int slot, InventoryObject object) {
        object.start();
        playerView.getPlayer().ifPresent(player -> {
            if (player.getHeldSlot() == slot && object instanceof Equipment equipment) {
                equipment.setSelected(true);
            }
        });
    }

    private InventoryObjectGroup getGroup(InventoryAccess access, Key groupKey) {
        InventoryObjectGroup group = access.groups().get(groupKey);
        if (group == null) {
            throw new IllegalArgumentException("Group " + groupKey + " does not exist");
        }

        return group;
    }

    private void applyTo(InventoryAccess newAccess, InventoryAccess oldAccess) {
        playerView.getPlayer().ifPresent(player -> {
            player.getInventory().clear();

            if (oldAccess != null) {
                InventoryProfile oldProfile = oldAccess.profile();
                for (int slot = 0; slot < oldProfile.getSlotCount(); slot++) {
                    if (!oldProfile.hasInventoryObject(slot)) {
                        continue;
                    }

                    InventoryObject object = oldProfile.getInventoryObject(slot);
                    if (slot == player.getHeldSlot() && object instanceof Equipment equipment) {
                        equipment.setSelected(false);
                    }

                    object.end();
                }
            }

            if (newAccess != null) {
                InventoryProfile newProfile = newAccess.profile();
                for (int slot = 0; slot < newProfile.getSlotCount(); slot++) {
                    if (!newProfile.hasInventoryObject(slot)) {
                        continue;
                    }

                    InventoryObject inventoryObject = newProfile.getInventoryObject(slot);
                    inventoryObject.start();

                    //don't ask for redraw when we initially set the item
                    player.getInventory().setItemStack(slot, inventoryObject.getItemStack());

                    if (slot == player.getHeldSlot() && inventoryObject instanceof Equipment equipment) {
                        equipment.setSelected(true);
                    }
                }
            }
        });
    }

}
