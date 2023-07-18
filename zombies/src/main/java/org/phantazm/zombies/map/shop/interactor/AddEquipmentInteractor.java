package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.Equipment;
import org.phantazm.core.equipment.EquipmentHandler;
import org.phantazm.core.inventory.*;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.List;
import java.util.Optional;

@Model("zombies.map.shop.interactor.add_equipment")
@Cache(false)
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
    public void initialize(@NotNull Shop shop) {
        ShopInteractor.initialize(successInteractors, shop);
        ShopInteractor.initialize(failureInteractors, shop);
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        boolean result = addOrReplaceEquipment(interaction);
        List<ShopInteractor> interactors = result ? successInteractors : failureInteractors;
        return result & ShopInteractor.handle(interactors, interaction);
    }

    private boolean addOrReplaceEquipment(PlayerInteraction interaction) {
        ZombiesPlayer zombiesPlayer = interaction.player();
        EquipmentHandler handler = zombiesPlayer.module().getEquipmentHandler();

        if (!data.allowDuplicate && handler.hasEquipment(data.groupKey, data.equipmentKey)) {
            return false;
        }

        boolean addEquipment;
        if (!(addEquipment = handler.canAddEquipment(data.groupKey)) && !data.allowReplace) {
            return false;
        }

        InventoryAccessRegistry accessRegistry = handler.accessRegistry();
        Optional<InventoryAccess> inventoryAccessOptional = accessRegistry.getCurrentAccess();
        if (inventoryAccessOptional.isEmpty()) {
            return false;
        }

        InventoryAccess inventoryAccess = inventoryAccessOptional.get();
        if (addEquipment) {
            Optional<Equipment> equipmentOptional = createEquipment(zombiesPlayer);
            if (equipmentOptional.isEmpty()) {
                return false;
            }

            Equipment equipment = equipmentOptional.get();
            if (data.specificSlot < 0) {
                handler.addEquipment(equipment, data.groupKey);
                return true;
            }

            InventoryObjectGroup group = inventoryAccess.groups().get(data.groupKey);
            if (invalidGroupSlot(group, data.specificSlot)) {
                return false;
            }

            InventoryProfile profile = group.getProfile();
            if (!profile.hasInventoryObject(data.specificSlot)) {
                accessRegistry.replaceObject(data.specificSlot, equipment);
                return true;
            }

            InventoryObject currentObject = profile.getInventoryObject(data.specificSlot);
            if (data.allowReplace || group.defaultObject() == currentObject) {
                accessRegistry.replaceObject(data.specificSlot, equipment);
                return true;
            }

            return false;
        }

        Optional<Player> playerOptional = zombiesPlayer.getPlayer();
        if (playerOptional.isEmpty()) {
            return false;
        }

        Player player = playerOptional.get();
        int targetSlot = data.specificSlot < 0 ? player.getHeldSlot() : data.specificSlot;

        InventoryObjectGroup group = inventoryAccess.groups().get(data.groupKey);
        if (invalidGroupSlot(group, targetSlot)) {
            return false;
        }

        Optional<Equipment> equipmentOptional = createEquipment(zombiesPlayer);
        if (equipmentOptional.isEmpty()) {
            return false;
        }

        Equipment equipment = equipmentOptional.get();
        accessRegistry.replaceObject(targetSlot, equipment);
        return true;
    }

    private boolean invalidGroupSlot(InventoryObjectGroup group, int slot) {
        return group == null || !group.getSlots().contains(slot);
    }

    @Override
    public void tick(long time) {
        ShopInteractor.tick(successInteractors, time);
        ShopInteractor.tick(failureInteractors, time);
    }

    private Optional<Equipment> createEquipment(ZombiesPlayer zombiesPlayer) {
        return zombiesPlayer.module().getEquipmentCreator().createEquipment(data.equipmentKey);
    }

    @DataObject
    public record Data(@NotNull Key equipmentKey,
                       @NotNull Key groupKey,
                       boolean allowReplace,
                       boolean allowDuplicate,
                       int specificSlot,
                       @NotNull @ChildPath("success") List<String> successInteractors,
                       @NotNull @ChildPath("failure") List<String> failureInteractors) {
    }
}
