package org.phantazm.core.item;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.gui.Gui;

import java.util.Objects;

@Model("item.updating.static")
@Cache
public class StaticUpdatingItem implements UpdatingItem {
    private final Data data;

    @FactoryMethod
    public StaticUpdatingItem(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull ItemStack update(@NotNull Gui gui, long time, @NotNull ItemStack current) {
        return data.item;
    }

    @Override
    public boolean hasUpdate(@NotNull Gui gui, long time, @NotNull ItemStack current) {
        return !current.equals(data.item);
    }

    @Override
    public @NotNull ItemStack currentItem() {
        return data.item;
    }

    @DataObject
    public record Data(@NotNull ItemStack item) {

    }
}
