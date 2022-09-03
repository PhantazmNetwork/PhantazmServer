package com.github.phantazmnetwork.zombies.game.perk;

import com.github.phantazmnetwork.core.item.UpdatingItem;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.upgrade.UpgradeNodeDataProcessor;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Model("zombies.perk.level.extra_health")
public class ExtraHealthLevel extends PerkLevelBase<ExtraHealthLevel.Data> {
    private static final float BASE_HEALTH = 20F;

    @ProcessorMethod
    public static ConfigProcessor<Data> processor() {
        return new UpgradeNodeDataProcessor<>() {
            @Override
            public @NotNull Data upgradeNodeDataFromNode(@NotNull ConfigNode node, @NotNull Key levelKey,
                    @NotNull Set<Key> upgrades) throws ConfigProcessException {
                int bonusHealth = node.getNumberOrThrow("bonusHealth").intValue();
                String updatingItem = node.getStringOrThrow("updatingItem");
                return new Data(levelKey, upgrades, bonusHealth, updatingItem);
            }

            @Override
            public @NotNull ConfigNode nodeFromUpgradeNodeData(Data data) {
                return ConfigNode.of("bonusHealth", data.bonusHealth, "updatingItem", data.updatingItem);
            }
        };
    }

    @FactoryMethod
    public ExtraHealthLevel(@NotNull Data data, @NotNull @DataName("updating_item") UpdatingItem item) {
        super(data, item);
    }

    @Override
    public void start(@NotNull ZombiesPlayer zombiesPlayer) {
        //TODO: API for modifying health on ZombiesPlayer?
        zombiesPlayer.getModule().getPlayerView().getPlayer()
                .ifPresent(player -> player.getLivingEntityMeta().setHealth(BASE_HEALTH + data.bonusHealth));
    }

    @Override
    public void end(@NotNull ZombiesPlayer zombiesPlayer) {
        zombiesPlayer.getModule().getPlayerView().getPlayer()
                .ifPresent(player -> player.getLivingEntityMeta().setHealth(BASE_HEALTH));
    }

    @DataObject
    public record Data(@NotNull Key levelKey,
                       @NotNull Set<Key> upgrades,
                       int bonusHealth,
                       @NotNull @DataPath("updating_item") String updatingItem) implements PerkData {

    }
}
