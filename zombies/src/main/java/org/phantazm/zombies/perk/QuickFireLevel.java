package org.phantazm.zombies.perk;

import com.github.steanky.element.core.annotation.ChildPath;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.item.UpdatingItem;
import org.phantazm.zombies.equipment.Equipment;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.shoot.ShootTester;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.*;

public class QuickFireLevel extends PerkLevelBase {

    private final Collection<ShootTester> registeredTesters = Collections.newSetFromMap(new IdentityHashMap<>());

    private final ZombiesPlayer user;

    public QuickFireLevel(@NotNull Data data, @NotNull UpdatingItem item, @NotNull ZombiesPlayer user) {
        super(data, item);
        this.user = Objects.requireNonNull(user, "user");
    }

    @Override
    protected @NotNull Data getData() {
        return (Data)super.getData();
    }

    @Override
    public void start() {

    }

    @Override
    public void tick(long time) {
        for (Equipment equipment : user.getModule().getEquipment()) {
            if (!(equipment instanceof Gun gun) || registeredTesters.contains(gun.getLevel().shootTester())) {
                continue;
            }

            gun.getLevel().shootTester().setShootSpeedMultiplier(getData().multiplier());
            registeredTesters.add(gun.getLevel().shootTester());
        }
    }

    @Override
    public void end() {
        for (ShootTester shootTester : registeredTesters) {
            shootTester.setShootSpeedMultiplier(1.0);
        }
    }

    @Model("zombies.perk.level.quick_fire")
    public record Creator(@NotNull Data data, @NotNull @ChildPath("updating_item") UpdatingItem item)
            implements PerkCreator {

        @FactoryMethod
        public Creator {

        }

        @Override
        public PerkLevel createPerk(@NotNull ZombiesPlayer user) {
            return new QuickFireLevel(data, item, user);
        }
    }

    @DataObject
    public record Data(@NotNull Key levelKey,
                       @NotNull Set<Key> upgrades,
                       @NotNull @ChildPath("updating_item") String updatingItemPath,
                       double multiplier) implements PerkData {

    }
}
