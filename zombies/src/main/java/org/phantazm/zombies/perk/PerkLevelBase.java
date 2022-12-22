package org.phantazm.zombies.perk;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.item.UpdatingItem;
import org.phantazm.zombies.upgrade.UpgradeNodeBase;
import org.phantazm.zombies.upgrade.UpgradeNodeData;

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
