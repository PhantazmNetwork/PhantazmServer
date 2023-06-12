package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.toolkit.collection.Wrapper;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.map.shop.PlayerInteraction;

@Model("zombies.map.shop.interactor.refill_ammo")
@Cache(false)
public class RefillAmmoInteractor extends InteractorBase<RefillAmmoInteractor.Data> {
    @FactoryMethod
    public RefillAmmoInteractor(@NotNull Data data) {
        super(data);
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        Wrapper<Boolean> result = Wrapper.of(false);

        if (data.onlyHeld) {
            interaction.player().getHeldEquipment().ifPresent(equipment -> {
                if (equipment instanceof Gun gun && (!data.matchKey || equipment.key().equals(data.equipmentKey))) {
                    gun.refill();
                    result.set(true);
                }
            });
        }
        else {
            interaction.player().module().getEquipmentHandler().accessRegistry().getCurrentAccess()
                    .ifPresent(access -> {
                        for (InventoryObject object : access.profile().objects()) {
                            if (object instanceof Gun gun && (!data.matchKey || gun.key().equals(data.equipmentKey))) {
                                gun.refill();
                                result.set(true);
                            }
                        }
                    });
        }

        return result.get();
    }

    @DataObject
    public record Data(@NotNull Key equipmentKey, boolean onlyHeld, boolean matchKey) {
    }
}
