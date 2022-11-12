package com.github.phantazmnetwork.zombies.map.objects;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.core.VecUtils;
import com.github.phantazmnetwork.zombies.coin.ModifierSource;
import com.github.phantazmnetwork.zombies.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.map.MapSettingsInfo;
import com.github.steanky.element.core.annotation.DependencySupplier;
import com.github.steanky.element.core.annotation.Memoize;
import com.github.steanky.element.core.dependency.DependencyModule;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class MapObjectDependencyModule implements DependencyModule {

    private final ZombiesMap map;

    private final Map<UUID, ZombiesPlayer> zombiesPlayers;

    public MapObjectDependencyModule(@NotNull ZombiesMap map, @NotNull Map<UUID, ZombiesPlayer> zombiesPlayers) {
        this.map = Objects.requireNonNull(map, "map");
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
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

    @DependencySupplier("java.map")
    @Memoize
    public @NotNull Map<UUID, ZombiesPlayer> zombiesPlayers() {
        return zombiesPlayers;
    }

    @DependencySupplier("java.collection")
    @Memoize
    public @NotNull Collection<ZombiesPlayer> zombiesPlayersCollection() {
        return zombiesPlayers.values();
    }

}
