package com.github.phantazmnetwork.zombies.game.perk;

import com.github.phantazmnetwork.core.item.UpdatingItem;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Model("zombies.perk.level.extra_health")
@Cache(false)
public class ExtraHealthLevel extends PerkLevelBase<ExtraHealthLevel.Data> {

    @FactoryMethod
    public ExtraHealthLevel(@NotNull Data data, @NotNull @DataName("updating_item") UpdatingItem item) {
        super(data, item);
    }

    @Override
    public void start(@NotNull ZombiesPlayer player) {

    }

    @Override
    public void end(@NotNull ZombiesPlayer player) {

    }

    public record Data(@NotNull Key levelKey,
                       @NotNull Set<Key> upgrades,
                       @NotNull @DataPath("updating_item") String updatingItem) implements PerkData {

    }
}
