package org.phantazm.zombies.equipment.perk.level;

import net.kyori.adventure.key.Key;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.zombies.equipment.perk.UpgradeData;
import org.phantazm.zombies.equipment.perk.visual.PerkVisual;

import java.util.Objects;
import java.util.Set;

public abstract class PerkLevelBase<TData extends UpgradeData> implements PerkLevel {
    protected final Key equipmentKey;
    protected final TData data;
    protected final PerkVisual perkVisual;

    public PerkLevelBase(@NotNull Key equipmentKey, @NotNull TData data, @NotNull PerkVisual perkVisual) {
        this.equipmentKey = Objects.requireNonNull(equipmentKey, "equipmentKey");
        this.data = Objects.requireNonNull(data, "data");
        this.perkVisual = Objects.requireNonNull(perkVisual, "perkVisual");
    }

    @Override
    public @Unmodifiable @NotNull Set<Key> upgrades() {
        return data.upgrades();
    }

    @Override
    public void setSelected(boolean selected) {

    }

    @Override
    public void rightClick() {

    }

    @Override
    public void leftClick() {

    }

    @Override
    public @NotNull Key key() {
        return equipmentKey;
    }

    @Override
    public @NotNull ItemStack getItemStack() {
        return perkVisual.computeItemStack();
    }

    @Override
    public boolean shouldRedraw() {
        return perkVisual.shouldCompute();
    }
}
