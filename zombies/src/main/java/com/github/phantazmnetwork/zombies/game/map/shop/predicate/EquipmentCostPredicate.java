package com.github.phantazmnetwork.zombies.game.map.shop.predicate;

import com.github.phantazmnetwork.commons.Prioritized;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.ElementData;
import com.github.steanky.element.core.annotation.ElementModel;
import com.github.steanky.element.core.annotation.FactoryMethod;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

@ElementModel("zombies.map.shop.predicate.equipment_cost")
public class EquipmentCostPredicate extends PredicateBase<EquipmentCostPredicate.Data> {
    @FactoryMethod
    public EquipmentCostPredicate(@NotNull Data data) {
        super(data);
    }

    @Override
    public boolean canHandleInteraction(@NotNull PlayerInteraction interaction) {
        return false;
    }

    @ElementData
    public record Data(int priority, @NotNull Key equipment, @NotNull Object2IntMap<Key> upgradeCosts)
            implements Prioritized {
    }

}
