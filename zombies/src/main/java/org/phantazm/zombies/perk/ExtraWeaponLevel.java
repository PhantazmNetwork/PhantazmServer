package org.phantazm.zombies.perk;

import com.github.steanky.element.core.annotation.ChildPath;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.inventory.InventoryObjectGroup;
import org.phantazm.core.item.UpdatingItem;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;
import java.util.Set;

public class ExtraWeaponLevel extends PerkLevelBase {

    private ZombiesPlayer user;

    public ExtraWeaponLevel(@NotNull Data data, @NotNull UpdatingItem item, @NotNull ZombiesPlayer user) {
        super(data, item);
        this.user = Objects.requireNonNull(user, "user");
    }

    @Override
    protected @NotNull Data getData() {
        return (Data)super.getData();
    }

    @Override
    public void start() {
        InventoryObjectGroup group =
                user.getModule().getInventoryAccessRegistry().getCurrentAccess().groups().get(getData().groupKey());
        if (group != null) {
            group.addSlot(getData().additionalSlot());
        }
    }

    @Override
    public void end() {
        InventoryObjectGroup group =
                user.getModule().getInventoryAccessRegistry().getCurrentAccess().groups().get(getData().groupKey());
        if (group != null) {
            group.popInventoryObject();
            group.removeSlot(getData().additionalSlot());
        }
    }

    @Model("zombies.perk.level.extra_weapon")
    public record Creator(@NotNull Data data, @NotNull UpdatingItem updatingItem) implements PerkCreator {

        @FactoryMethod
        public Creator {

        }

        @Override
        public PerkLevel createPerk(@NotNull ZombiesPlayer user) {
            return new ExtraWeaponLevel(data, updatingItem, user);
        }
    }

    @DataObject
    public record Data(@NotNull Key levelKey,
                       @NotNull Set<Key> upgrades,
                       @NotNull @ChildPath("updating_item") String updatingItemPath,
                       @NotNull Key groupKey,
                       int additionalSlot) implements PerkData {

    }

}
