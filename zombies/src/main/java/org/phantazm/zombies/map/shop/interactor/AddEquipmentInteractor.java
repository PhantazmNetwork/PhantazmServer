package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.toolkit.collection.Wrapper;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.EquipmentHandler;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.player.ZombiesPlayer;

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

    private boolean addEquipment(ZombiesPlayer player) {
        EquipmentHandler handler = player.module().getEquipmentHandler();
        if (handler.canAddEquipment(data.groupKey)) {
            Wrapper<Boolean> wrapper = Wrapper.of(false);
            player.module().getEquipmentCreator().createEquipment(data.equipmentKey).ifPresent(value -> {
                wrapper.set(true);
                handler.addEquipment(value, data.groupKey);
            });

            return wrapper.get();
        }

        return false;
    }

    @DataObject
    public record Data(@NotNull Key equipmentKey, @NotNull Key groupKey) {
    }
}
