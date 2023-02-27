package org.phantazm.zombies.equipment.perk.effect;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.annotation.document.Description;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.EquipmentHandler;
import org.phantazm.core.inventory.InventoryObjectGroup;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;

@Description("""
        An effect that adds some additional slots to specific equipment groups. Equipment groups for all players are
        defined in the settings file for each particular map.
        """)
@Model("zombies.perk.effect.add_group_slots")
@Cache(false)
public class AddGroupSlotsCreator implements PerkEffectCreator {
    private final Data data;

    @FactoryMethod
    public AddGroupSlotsCreator(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @NotNull PerkEffect forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        return new Effect(data, zombiesPlayer.module().getEquipmentHandler());
    }

    private static class Effect implements PerkEffect {
        private final Data data;
        private final EquipmentHandler handler;

        private Effect(Data data, EquipmentHandler handler) {
            this.data = data;
            this.handler = handler;
        }

        @Override
        public void start() {
            handler.accessRegistry().getCurrentAccess().ifPresent(inventoryAccess -> {
                InventoryObjectGroup group = inventoryAccess.groups().get(data.group);
                if (group == null) {
                    return;
                }

                IntSet existingSlots = group.getSlots();
                for (int slot : data.additionalSlots) {
                    if (!existingSlots.contains(slot)) {
                        group.addSlot(slot);
                    }
                }
            });

            handler.refreshGroup(data.group);
        }

        @Override
        public void end() {
            handler.accessRegistry().getCurrentAccess().ifPresent(inventoryAccess -> {
                InventoryObjectGroup group = inventoryAccess.groups().get(data.group);
                if (group == null) {
                    return;
                }

                IntSet existingSlots = group.getSlots();
                for (int slot : data.additionalSlots) {
                    if (existingSlots.contains(slot)) {
                        group.removeSlot(slot);
                    }
                }
            });

            handler.refreshGroup(data.group);
        }
    }

    @DataObject
    public record Data(@NotNull Key group, @NotNull IntSet additionalSlots) {

    }
}
