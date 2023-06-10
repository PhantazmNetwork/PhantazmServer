package org.phantazm.core.equipment;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Model("zombies.map.shop.upgrade_path.none")
@Cache
public class NoUpgradePath implements UpgradePath {
    @FactoryMethod
    public NoUpgradePath() {
    }

    @Override
    public @NotNull Optional<Key> nextUpgrade(@NotNull Key key) {
        return Optional.empty();
    }
}
