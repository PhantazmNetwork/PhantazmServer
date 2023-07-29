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
import org.phantazm.core.inventory.InventoryAccessRegistry;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.core.inventory.InventoryObjectGroup;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;
import java.util.Set;

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
            handler.accessRegistry().getCurrentAccess().ifPresent(access -> {
                InventoryObjectGroup group = access.groups().get(data.group);
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
            InventoryAccessRegistry accessRegistry = handler.accessRegistry();

            accessRegistry.getCurrentAccess().ifPresent(access -> {
                InventoryObjectGroup group = access.groups().get(data.group);
                if (group == null) {
                    return;
                }

                IntSet existingSlots = group.getSlots();
                for (int slot : data.additionalSlots) {
                    if (existingSlots.contains(slot)) {
                        group.removeSlot(slot);

                        InventoryObject defaultObject = group.defaultObject();
                        if (defaultObject == null) {
                            accessRegistry.removeObject(access, slot);
                        }
                        else {
                            accessRegistry.replaceObject(access, slot, defaultObject);
                        }
                    }
                }
            });

            handler.refreshGroup(data.group);
        }
    }

    @DataObject
    public record Data(@NotNull @Description("The equipment group key") Key group,
                       @NotNull @Description(
                               "The additional slots to add to this group") Set<Integer> additionalSlots) {

    }
}
