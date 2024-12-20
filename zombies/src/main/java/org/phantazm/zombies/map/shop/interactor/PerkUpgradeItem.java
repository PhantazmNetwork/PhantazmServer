package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.core.item.UpdatingItem;

import java.util.List;

@Model("item.updating.perk_upgrade")
@Cache(false)
public class PerkUpgradeItem implements UpdatingItem {
    private static final int UPDATE_INTERVAL = 10;

    private final Data data;

    private ItemStack itemStack;
    private int ticks;

    @FactoryMethod
    public PerkUpgradeItem(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull ItemStack update(long time, @NotNull ItemStack current) {
        return itemStack;
    }

    @Override
    public boolean hasUpdate(long time, @NotNull ItemStack current) {
        return (ticks++ % UPDATE_INTERVAL == 0);
    }

    @Override
    public @NotNull ItemStack currentItem() {
        return itemStack;
    }

    @Default("""
        {
          displayName=null,
          lore=null,
          tag=null
        }
        """)
    @DataObject
    public record Data(
        @NotNull Material material,
        @Nullable String displayName,
        @Nullable List<String> lore,
        @Nullable String tag) {
    }
}
