package com.github.phantazmnetwork.zombies.perk;

import com.github.phantazmnetwork.core.item.UpdatingItem;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Model("zombies.perk.level.extra_health")
public class ExtraHealthLevel extends PerkLevelBase {
    private static final float BASE_HEALTH = 20F;

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
        zombiesPlayer.getPlayer()
                .ifPresent(player -> player.getLivingEntityMeta().setHealth(BASE_HEALTH + getData().bonusHealth));
    }

    @Override
    public void end(@NotNull ZombiesPlayer zombiesPlayer) {
        zombiesPlayer.getPlayer().ifPresent(player -> player.getLivingEntityMeta().setHealth(BASE_HEALTH));
    }

    @DataObject
    public record Data(@NotNull Key levelKey,
                       @NotNull Set<Key> upgrades,
                       int bonusHealth,
                       @NotNull @DataPath("updating_item") String updatingItem) implements PerkData {

    }
}
