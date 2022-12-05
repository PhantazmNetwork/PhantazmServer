package com.github.phantazmnetwork.zombies.map.shop;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Model("zombies.map.shop.upgrade_path.linear")
public class LinearUpgradePath implements UpgradePath {
    private final Data data;

    @FactoryMethod
    public LinearUpgradePath(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @NotNull Optional<Key> nextUpgrade(@NotNull Key key) {
        return Optional.ofNullable(data.upgrades.get(key));
    }

    @DataObject
    public record Data(@NotNull Map<Key, Key> upgrades) {

    }
}
