package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.toolkit.collection.Wrapper;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.Equipment;
import org.phantazm.core.equipment.EquipmentHandler;
import org.phantazm.core.inventory.InventoryAccessRegistry;
import org.phantazm.core.inventory.InventoryObjectGroup;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Map;
import java.util.Optional;

@Model("zombies.map.shop.interactor.add_equipment")
@Cache
public class AddEquipmentInteractor extends InteractorBase<AddEquipmentInteractor.Data> {
    @FactoryMethod
    public AddEquipmentInteractor(@NotNull Data data) {
        super(data);
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        return addEquipment(interaction.player());
    }

    private boolean addEquipment(ZombiesPlayer zombiesPlayer) {
        EquipmentHandler handler = zombiesPlayer.module().getEquipmentHandler();

        Wrapper<Boolean> wrapper = Wrapper.of(false);
        if (handler.canAddEquipment(data.groupKey)) {
            createEquipment(zombiesPlayer).ifPresent(equipment -> {
                wrapper.set(true);
                handler.addEquipment(equipment, data.groupKey);
            });
        }
        else if (data.allowReplace) {
            zombiesPlayer.getPlayer().ifPresent(player -> {
                int heldSlot = player.getHeldSlot();

                InventoryAccessRegistry accessRegistry = handler.accessRegistry();
                accessRegistry.getCurrentAccess().ifPresent(inventoryAccess -> {
                    Map.Entry<Key, InventoryObjectGroup> targetEntry = null;
                    for (Map.Entry<Key, InventoryObjectGroup> entry : inventoryAccess.groups().entrySet()) {
                        if (entry.getValue().getSlots().contains(heldSlot)) {
                            targetEntry = entry;
                            break;
                        }
                    }

                    if (targetEntry != null && targetEntry.getKey().equals(data.groupKey)) {
                        if (targetEntry.getValue().getProfile().hasInventoryObject(heldSlot)) {
                            createEquipment(zombiesPlayer).ifPresent(equipment -> {
                                wrapper.set(true);
                                accessRegistry.replaceObject(heldSlot, equipment);
                            });
                        }
                    }
                });
            });
        }

        return wrapper.get();
    }

    private Optional<Equipment> createEquipment(ZombiesPlayer zombiesPlayer) {
        return zombiesPlayer.module().getEquipmentCreator().createEquipment(data.equipmentKey);
    }

    @DataObject
    public record Data(@NotNull Key equipmentKey, @NotNull Key groupKey, boolean allowReplace) {
    }
}
