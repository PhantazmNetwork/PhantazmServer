package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.core.VecUtils;
import com.github.phantazmnetwork.zombies.game.coin.ModifierSource;
import com.github.phantazmnetwork.zombies.map.MapSettingsInfo;
import com.github.steanky.element.core.annotation.DependencySupplier;
import com.github.steanky.element.core.annotation.Memoize;
import com.github.steanky.element.core.dependency.DependencyModule;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
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

    @DependencySupplier("minestom.point.pos")
    @Memoize
    public Pos provideMinestomRespawnPoint(@NotNull Key key) {
        if (key.equals(Key.key(Namespaces.PHANTAZM, "zombies.map.respawn_point.minestom"))) {
            MapSettingsInfo settingsInfo = map.getData().settings();
            return Pos.fromPoint(VecUtils.toPoint(settingsInfo.origin().add(settingsInfo.spawn())));
        }

        return null;
    }

}
