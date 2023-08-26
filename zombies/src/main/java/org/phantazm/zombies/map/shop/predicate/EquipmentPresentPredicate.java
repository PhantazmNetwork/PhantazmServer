package org.phantazm.zombies.map.shop.predicate;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

@Model("zombies.map.shop.equipment_predicate.present")
@Cache(false)
public class EquipmentPresentPredicate extends PredicateBase<EquipmentPresentPredicate.Data> {

    @FactoryMethod
    public EquipmentPresentPredicate(@NotNull Data data) {
        super(data);
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction, @NotNull Shop shop) {
        return data.requirePresent == isPresent(interaction);
    }

    private boolean isPresent(PlayerInteraction interaction) {
        if (data.requireHeld) {
            return interaction.player().getHeldEquipment().map(equipment -> equipment.key().equals(data.equipmentKey))
                .orElse(false);
        }

        return interaction.player().module().getEquipmentHandler().hasEquipment(data.equipmentGroup, data.equipmentKey);
    }

    @DataObject
    public record Data(
        @NotNull Key equipmentGroup,
        @NotNull Key equipmentKey,
        boolean requirePresent,
        boolean requireHeld) {
    }
}
