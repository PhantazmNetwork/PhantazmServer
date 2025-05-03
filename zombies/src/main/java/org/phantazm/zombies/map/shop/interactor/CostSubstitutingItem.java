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
import org.phantazm.core.gui.ItemUpdater;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.coin.TransactionModifierSource;

import java.util.List;

@Model("item.updating.cost_substituting")
@Cache(false)
public class CostSubstitutingItem implements ItemUpdater {
    private final Data data;
    private final TransactionModifierSource modifierSource;

    @FactoryMethod
    public CostSubstitutingItem(@NotNull Data data, @NotNull TransactionModifierSource modifierSource) {
        this.data = data;
        this.modifierSource = modifierSource;
    }

    @Override
    public @NotNull ItemStack update(@NotNull Gui gui, long time, @NotNull ItemStack current, int slot) {
        return computeItemStack(cost());
    }

    @Override
    public boolean hasUpdate(@NotNull Gui gui, long time, @NotNull ItemStack current, int slot) {
        return !computeItemStack(cost()).equals(current);
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
