package org.phantazm.zombies.perk;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.item.UpdatingItem;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Set;

@Model("zombies.perk.level.extra_health")
public class ExtraHealthLevel extends PerkLevelBase {
    @FactoryMethod
    public ExtraHealthLevel(@NotNull Data data, @NotNull @DataName("updating_item") UpdatingItem item) {
        super(data, item);
    }

    @Override
    protected @NotNull Data getData() {
        return (Data)super.getData();
    }

    @Override
    public void start(@NotNull ZombiesPlayer zombiesPlayer) {
        //TODO: API for modifying health on ZombiesPlayer?
        zombiesPlayer.getPlayer().ifPresent(player -> {
            LivingEntityMeta meta = player.getLivingEntityMeta();
            meta.setHealth(meta.getHealth() + getData().bonusHealth);
        });
    }

    @Override
    public void end(@NotNull ZombiesPlayer zombiesPlayer) {
        zombiesPlayer.getPlayer().ifPresent(player -> {
            LivingEntityMeta meta = player.getLivingEntityMeta();
            meta.setHealth(meta.getHealth() - getData().bonusHealth);
        });
    }

    @DataObject
    public record Data(@NotNull Key levelKey,
                       @NotNull Set<Key> upgrades,
                       int bonusHealth,
                       @NotNull @DataPath("updating_item") String updatingItem) implements PerkData {

    }
}
