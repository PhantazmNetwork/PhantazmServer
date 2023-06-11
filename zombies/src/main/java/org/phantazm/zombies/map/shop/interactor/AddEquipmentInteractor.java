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

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Model("zombies.map.shop.interactor.add_equipment")
@Cache
public class AddEquipmentInteractor extends InteractorBase<AddEquipmentInteractor.Data> {
    private final List<ShopInteractor> successInteractors;
    private final List<ShopInteractor> failureInteractors;

    @FactoryMethod
    public AddEquipmentInteractor(@NotNull Data data,
            @NotNull @Child("success") List<ShopInteractor> successInteractors,
            @NotNull @Child("failure") List<ShopInteractor> failureInteractors) {
        super(data);
        this.successInteractors = successInteractors;
        this.failureInteractors = failureInteractors;
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        return addEquipment(interaction);
    }

    private boolean addEquipment(PlayerInteraction interaction) {
        ZombiesPlayer zombiesPlayer = interaction.player();
        EquipmentHandler handler = zombiesPlayer.module().getEquipmentHandler();

        Wrapper<Boolean> wrapper = Wrapper.of(false);
        if (data.allowDuplicate || !handler.hasEquipment(data.groupKey, data.equipmentKey)) {
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
                            createEquipment(zombiesPlayer).ifPresent(equipment -> {
                                wrapper.set(true);
                                accessRegistry.replaceObject(heldSlot, equipment);
                            });
                        }
                    });
                });
            }
        }

        boolean success = wrapper.get();
        if (success) {
            for (ShopInteractor interactor : successInteractors) {
                success &= interactor.handleInteraction(interaction);
            }

            return success;
        }

        for (ShopInteractor interactor : failureInteractors) {
            interactor.handleInteraction(interaction);
        }

        return false;
    }

    private Optional<Equipment> createEquipment(ZombiesPlayer zombiesPlayer) {
        return zombiesPlayer.module().getEquipmentCreator().createEquipment(data.equipmentKey);
    }

    @DataObject
    public record Data(@NotNull Key equipmentKey,
                       @NotNull Key groupKey,
                       boolean allowReplace,
                       boolean allowDuplicate,
                       @NotNull @ChildPath("success") List<String> successInteractors,
                       @NotNull @ChildPath("failure") List<String> failureInteractors) {
    }
}
