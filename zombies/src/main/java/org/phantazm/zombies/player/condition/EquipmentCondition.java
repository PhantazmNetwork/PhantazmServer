package org.phantazm.zombies.player.condition;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.function.Predicate;

@Model("zombies.player.condition.has_equipment")
@Cache(false)
public class EquipmentCondition implements Predicate<ZombiesPlayer> {
    private final Data data;

    @FactoryMethod
    public EquipmentCondition(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public boolean test(ZombiesPlayer zombiesPlayer) {
        return zombiesPlayer.module().getEquipmentHandler().hasEquipment(data.groupKey, data.equipmentKey);
    }

    @DataObject
    public record Data(@NotNull Key groupKey,
        @NotNull Key equipmentKey) {
    }
}
