package com.github.phantazmnetwork.zombies.perk;

import com.github.phantazmnetwork.core.item.UpdatingItem;
import com.github.phantazmnetwork.zombies.upgrade.UpgradeNodeBase;
import com.github.phantazmnetwork.zombies.upgrade.UpgradeNodeData;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class PerkLevelBase extends UpgradeNodeBase implements PerkLevel {
    protected final UpdatingItem item;

    public PerkLevelBase(@NotNull UpgradeNodeData data, @NotNull UpdatingItem item) {
        super(data);
        this.item = item;
    }

    @Override
    public @NotNull ItemStack getItemStack() {
        return item.currentItem();
    }

    @Override
    public boolean shouldRedraw() {
        return item.hasUpdate(System.currentTimeMillis(), item.currentItem());
    }

    @Override
    public void tick(long time) {

    }
}
