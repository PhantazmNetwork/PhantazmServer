package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.core.ItemStackUtils;
import org.phantazm.core.gui.Gui;
import org.phantazm.core.item.UpdatingItem;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.coin.TransactionModifierSource;

import java.util.List;

@Model("item.updating.cost_substituting")
@Cache(false)
public class CostSubstitutingItem implements UpdatingItem {
    private static final int UPDATE_INTERVAL = 10;

    private final Data data;
    private final TransactionModifierSource modifierSource;

    private ItemStack itemStack;
    private int ticks;

    private boolean hasOldCost;
    private int oldCost;

    @FactoryMethod
    public CostSubstitutingItem(@NotNull Data data, @NotNull TransactionModifierSource modifierSource) {
        this.data = data;
        this.modifierSource = modifierSource;
        this.itemStack = computeItemStack(data.cost);
    }

    @Override
    public @NotNull ItemStack update(@NotNull Gui gui, long time, @NotNull ItemStack current) {
        int cost = cost();
        this.oldCost = cost;
        this.hasOldCost = true;
        return itemStack = computeItemStack(cost);
    }

    @Override
    public boolean hasUpdate(@NotNull Gui gui, long time, @NotNull ItemStack current) {
        return (ticks++ % UPDATE_INTERVAL == 0 && (!hasOldCost || cost() != oldCost));
    }

    @Override
    public @NotNull ItemStack currentItem() {
        return itemStack;
    }

    private ItemStack computeItemStack(int cost) {
        return ItemStackUtils.buildItem(data.material, data.tag, data.displayName, data.lore,
            Placeholder.unparsed("cost", Integer.toString(cost)));
    }

    private int cost() {
        int cost = data.cost;
        for (Transaction.Modifier modifier : modifierSource.modifiers(data.modifier)) {
            cost = modifier.modify(cost);
        }

        return cost;
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
        @Nullable String tag,
        int cost,
        @NotNull Key modifier) {
    }
}
