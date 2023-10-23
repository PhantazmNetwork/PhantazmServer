package org.phantazm.zombies.map.shop.predicate;

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
import org.phantazm.zombies.map.shop.Shop;

@Model("zombies.map.shop.predicate.gun_full")
@Cache(false)
public class GunFullPredicate extends PredicateBase<GunFullPredicate.Data> {

    @FactoryMethod
    public GunFullPredicate(@NotNull Data data) {
        super(data);
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction, @NotNull Shop shop) {
        Wrapper<Boolean> result = Wrapper.of(true);

        if (data.onlyHeld) {
            interaction.player().getHeldEquipment().ifPresent(equipment -> {
                if (equipment instanceof Gun gun && (!data.matchKey || equipment.key().equals(data.equipmentKey))) {
                    result.set(gun.getState().ammo() >= gun.getLevel().stats().maxAmmo());
                }
            });
        }
        else {
            interaction.player().module().getEquipmentHandler().accessRegistry().getCurrentAccess()
                    .ifPresent(access -> {
                        for (InventoryObject object : access.profile().objects()) {
                            if (object instanceof Gun gun && (!data.matchKey || gun.key().equals(data.equipmentKey))) {
                                if (gun.getState().ammo() < gun.getLevel().stats().maxAmmo()) {
                                    result.set(false);
                                }
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
