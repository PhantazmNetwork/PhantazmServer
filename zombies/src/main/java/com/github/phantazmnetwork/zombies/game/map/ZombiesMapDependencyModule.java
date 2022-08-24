package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.zombies.game.coin.ModifierSource;
import com.github.steanky.element.core.annotation.DependencySupplier;
import com.github.steanky.element.core.annotation.Memoize;
import com.github.steanky.element.core.dependency.DependencyModule;
import org.jetbrains.annotations.NotNull;

public class ZombiesMapDependencyModule implements DependencyModule {
    private final ZombiesMap map;

    public ZombiesMapDependencyModule(@NotNull ZombiesMap map) {
        this.map = map;
    }

    @DependencySupplier("zombies.dependency.map")
    @Memoize
    public @NotNull ZombiesMap provideMap() {
        return map;
    }

    @DependencySupplier("zombies.dependency.modifier_source")
    @Memoize
    public @NotNull ModifierSource modifierSource() {
        return map.modifierSource();
    }
}
