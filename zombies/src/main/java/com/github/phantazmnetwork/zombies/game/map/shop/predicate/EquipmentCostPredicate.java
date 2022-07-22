package com.github.phantazmnetwork.zombies.game.map.shop.predicate;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.Prioritized;
import com.github.phantazmnetwork.commons.component.annotation.ComponentData;
import com.github.phantazmnetwork.commons.component.annotation.ComponentFactory;
import com.github.phantazmnetwork.commons.component.annotation.ComponentModel;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

@ComponentModel("phantazm:zombies.map.shop.predicate.equipment_cost")
public class EquipmentCostPredicate extends PredicateBase<EquipmentCostPredicate.Data> {
    @ComponentFactory
    public EquipmentCostPredicate(@NotNull Data data) {
        super(data);
    }

    @Override
    public boolean canHandleInteraction(@NotNull PlayerInteraction interaction) {
        return false;
    }

    @ComponentData
    public record Data(int priority, @NotNull Key equipment, @NotNull Object2IntMap<Key> upgradeCosts)
            implements Keyed, Prioritized {
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "zombies.map.shop.predicate.equipment_cost");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

}
