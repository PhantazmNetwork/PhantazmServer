package com.github.phantazmnetwork.zombies.mapeditor.client;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.zombies.map.ZombiesMap;
import net.kyori.adventure.key.Key;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface MapeditorSession {
    @NotNull ActionResult handleBlockUse(@NotNull PlayerEntity player, @NotNull World world, @NotNull Hand hand,
                                         @NotNull BlockHitResult blockHitResult);

    void setEnabled(boolean enabled);

    boolean isEnabled();

    boolean hasSelection();

    @NotNull Vec3I getFirstSelection();

    @NotNull Vec3I getSecondSelection();

    boolean hasMap();

    @NotNull ZombiesMap getMap();

    void addMap(@NotNull Key id, @NotNull ZombiesMap map);

    boolean containsMap(@NotNull Key id);

    void removeMap(@NotNull Key id);

    @NotNull Map<Key, ZombiesMap> mapView();

    void setCurrent(@NotNull Key id);

    void loadMapsFromDisk();

    void saveMaps();
}
