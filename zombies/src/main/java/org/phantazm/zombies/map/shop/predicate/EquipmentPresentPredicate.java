package org.phantazm.zombies.map.shop.predicate;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.Equipment;
import org.phantazm.core.equipment.EquipmentHandler;
import org.phantazm.zombies.map.shop.PlayerInteraction;

import java.util.Collection;

@Model("zombies.map.shop.equipment_predicate.present")
@Cache(false)
public class EquipmentPresentPredicate extends PredicateBase<EquipmentPresentPredicate.Data> {

    @FactoryMethod
    public EquipmentPresentPredicate(@NotNull Data data) {
        super(data);
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        return data.requirePresent == isPresent(interaction);
    }

    private boolean isPresent(PlayerInteraction interaction) {
        EquipmentHandler handler = interaction.player().module().getEquipmentHandler();
        Collection<Equipment> equipmentCollection = handler.getEquipment(data.equipmentGroup);
        for (Equipment equipment : equipmentCollection) {
            if (equipment.key().equals(data.equipmentKey)) {
                return true;
            }
        }

        return false;
    }

    @DataObject
    public record Data(@NotNull Key equipmentGroup, @NotNull Key equipmentKey, boolean requirePresent) {
    }
}
