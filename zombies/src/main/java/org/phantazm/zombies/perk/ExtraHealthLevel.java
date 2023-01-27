package org.phantazm.zombies.perk;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.item.UpdatingItem;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;
import java.util.Set;

public class ExtraHealthLevel extends PerkLevelBase {

    private final ZombiesPlayer user;

    public ExtraHealthLevel(@NotNull Data data, @NotNull UpdatingItem item, @NotNull ZombiesPlayer user) {
        super(data, item);
        this.user = Objects.requireNonNull(user, "user");
    }

    @Override
    protected @NotNull Data getData() {
        return (Data)super.getData();
    }

    @Override
    public void start() {
        user.getPlayer().ifPresent(player -> {
            LivingEntityMeta meta = player.getLivingEntityMeta();
            meta.setHealth(meta.getHealth() + getData().bonusHealth);
        });
    }

    @Override
    public void end() {
        user.getPlayer().ifPresent(player -> {
            LivingEntityMeta meta = player.getLivingEntityMeta();
            meta.setHealth(meta.getHealth() - getData().bonusHealth);
        });
    }

    @Model("zombies.perk.level.extra_health")
    public record Creator(@NotNull Data data, @Child("updating_item") UpdatingItem item) implements PerkCreator {

        @FactoryMethod
        public Creator {

        }

        @Override
        public PerkLevel createPerk(@NotNull ZombiesPlayer user) {
            return new ExtraHealthLevel(data, item, user);
        }
    }

    @DataObject
    public record Data(@NotNull Key levelKey,
                       @NotNull Set<Key> upgrades,
                       int bonusHealth,
                       @NotNull @ChildPath("updating_item") String updatingItem) implements PerkData {

    }
}
