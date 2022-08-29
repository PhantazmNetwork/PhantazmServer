package com.github.phantazmnetwork.zombies.game.perk;

import com.github.phantazmnetwork.zombies.equipment.upgrade.UpgradeNodeBase;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class PerkLevelBase<TData extends PerkData> extends UpgradeNodeBase<TData> implements PerkLevel {
    protected ItemStack item;

    public PerkLevelBase(@NotNull TData data, @NotNull ItemStack item) {
        super(data);
        this.item = item;
    }

    @Override
    public @NotNull ItemStack getItemStack() {
        return item;
    }

    @Override
    public boolean shouldRedraw() {
        return false;
    }

    @Override
    public void start() {

    }

    @Override
    public void tick(long time) {

    }

    @Override
    public void end() {

    }
}
