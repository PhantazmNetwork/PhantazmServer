package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record MapInfo(@NotNull Key id,
                      @NotNull Vec3I origin,
                      float pitch,
                      float yaw,
                      @NotNull Component displayName,
                      @NotNull String displayItemTag,
                      @NotNull List<Component> introMessages,
                      @NotNull Component scoreboardHeader,
                      @NotNull Vec3I leaderboardPosition,
                      int leaderboardLength,
                      int worldTime,
                      int maxPlayers,
                      int minPlayers,
                      int startingCoins,
                      int repairCoins,
                      double windowRepairRadius,
                      int windowRepairTicks,
                      int corpseDeathTicks,
                      double reviveRadius,
                      boolean canWallshoot,
                      boolean perksLostOnDeath,
                      int baseReviveTicks,
                      int rollsPerChest,
                      @NotNull List<Integer> milestoneRounds,
                      @NotNull List<Key> defaultEquipment) {
    public MapInfo(@NotNull Key id, @NotNull Vec3I origin) {
        this(
                id,
                origin,
                0,
                0,
                Component.text(id.value()),
                "{id:\"stone\",Count:1,tag:{Name:\"New Map\"}}",
                new ArrayList<>(),
                Component.text(id.value()),
                origin,
                15,
                0,
                4,
                1,
                0,
                20,
                3,
                20,
                500,
                2,
                false,
                false,
                30,
                5,
                new ArrayList<>(),
                new ArrayList<>()
        );
    }
}
