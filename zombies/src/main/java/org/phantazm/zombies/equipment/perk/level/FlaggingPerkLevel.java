package org.phantazm.zombies.equipment.perk.level;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.annotation.document.Description;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.perk.visual.PerkVisualCreator;
import org.phantazm.zombies.equipment.perk.UpgradeData;
import org.phantazm.zombies.equipment.perk.visual.PerkVisual;
import org.phantazm.zombies.map.Flaggable;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;
import java.util.Set;

@Description("A perk that sets a flag on the player for the duration that it is active.")
@Model("zombies.perk.level.flagging")
@Cache(false)
public class FlaggingPerkLevel implements PerkLevelCreator {
    private final Data data;
    private final PerkVisualCreator perkVisualCreator;
    private final Key equipmentKey;

    @FactoryMethod
    public FlaggingPerkLevel(@NotNull Data data, @NotNull @Child("perk_visual") PerkVisualCreator perkVisualCreator,
            @NotNull Key equipmentKey) {
        this.data = Objects.requireNonNull(data, "data");
        this.perkVisualCreator = Objects.requireNonNull(perkVisualCreator, "perkVisualCreator");
        this.equipmentKey = Objects.requireNonNull(equipmentKey, "equipmentKey");
    }

    @Override
    public @NotNull PerkLevel forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        PerkVisual visual = perkVisualCreator.forPlayer(zombiesPlayer);
        return new Level(equipmentKey, data, visual, zombiesPlayer.flags());
    }

    private static class Level extends PerkLevelBase<Data> {
        private final Flaggable flaggable;

        private Level(@NotNull Key equipmentKey, @NotNull Data data, @NotNull PerkVisual perkVisual,
                @NotNull Flaggable flaggable) {
            super(equipmentKey, data, perkVisual);
            this.flaggable = flaggable;
        }

        @Override
        public void start() {
            flaggable.setFlag(data.flag);
        }

        @Override
        public void end() {
            flaggable.clearFlag(data.flag);
        }
    }

    @DataObject
    public record Data(@NotNull Set<Key> upgrades,
                       @NotNull Key flag,
                       @NotNull @ChildPath("perk_visual_creator") String perkVisual) implements UpgradeData {

    }
}
