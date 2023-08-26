package org.phantazm.core.item;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

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
    public @NotNull ItemStack update(long time, @NotNull ItemStack current) {
        return data.item;
    }

    @Override
    public boolean hasUpdate(long time, @NotNull ItemStack current) {
        return false;
    }

    @Override
    public @NotNull ItemStack currentItem() {
        return data.item;
    }

    @DataObject
    public record Data(@NotNull ItemStack item) {

    }
}
