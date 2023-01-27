package org.phantazm.zombies.perk;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.item.UpdatingItem;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;
import java.util.Set;


public class FastReviveLevel extends PerkLevelBase {

    private final ZombiesPlayer user;

    public FastReviveLevel(@NotNull Data data, @NotNull UpdatingItem item, @NotNull ZombiesPlayer user) {
        super(data, item);
        this.user = Objects.requireNonNull(user, "user");
    }

    @Override
    protected @NotNull Data getData() {
        return (Data)super.getData();
    }

    @Override
    public void start() {
        user.setReviveSpeedMultiplier(1.25);
    }

    @Override
    public void tick(long time) {
        super.tick(time);
    }

    @Override
    public void end() {
        user.setReviveSpeedMultiplier(1.0);
    }

    @Model("zombies.perk.level.fast_revive")
    public record Creator(@NotNull Data data, @NotNull @Child("updating_item") UpdatingItem item)
            implements PerkCreator {

        @FactoryMethod
        public Creator {

        }

        @Override
        public PerkLevel createPerk(@NotNull ZombiesPlayer user) {
            return new FastReviveLevel(data, item, user);
        }
    }

    @DataObject
    public record Data(@NotNull Key levelKey,
                       @NotNull Set<Key> upgrades,
                       double amount,
                       @NotNull @ChildPath("updating_item") String updatingItemPath) implements PerkData {

    }
}
