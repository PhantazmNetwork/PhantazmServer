package com.github.phantazmnetwork.zombies.game.map.shop.predicate;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.Prioritized;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.ElementData;
import com.github.steanky.element.core.annotation.ElementModel;
import com.github.steanky.element.core.annotation.FactoryMethod;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
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
            implements Keyed, Prioritized {
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "zombies.map.shop.predicate.equipment_cost");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

}
