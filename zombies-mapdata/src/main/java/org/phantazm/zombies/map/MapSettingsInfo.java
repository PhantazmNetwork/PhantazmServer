package org.phantazm.zombies.map;

import com.github.steanky.vector.Vec3I;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Defines the general settings for a map.
 */
public record MapSettingsInfo(int mapDataVersion,
                              @NotNull Key id,
                              @NotNull List<String> instancePath,
                              @NotNull Vec3I origin,
                              int minimumProtocolVersion,
                              int maximumProtocolVersion,
                              @NotNull Vec3I spawn,
                              float pitch,
                              float yaw,
                              @NotNull Component displayName,
                              @NotNull String displayItemSnbt,
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
                              double powerupPickupRadius,
                              long windowRepairTicks,
                              long corpseDeathTicks,
                              long healTicks,
                              double reviveRadius,
                              boolean canWallshoot,
                              boolean perksLostOnDeath,
                              long baseReviveTicks,
                              int rollsPerChest,
                              @NotNull List<Integer> milestoneRounds,
                              @NotNull List<Key> defaultEquipment) {

    public static final int MAP_DATA_VERSION = 1;

    /**
     * Constructs a new instance of this record.
     *
     * @param id                     the id of this map
     * @param origin                 the origin of this map; this is the point from which all other coordinates are relative to unless
     *                               otherwise specified
     * @param minimumProtocolVersion the minimum protocol version required to join this map (inclusive)
     * @param maximumProtocolVersion maximum protocol version required to join this map (inclusive)
     * @param spawn                  the spawn of the map, relative to origin
     * @param pitch                  the pitch that players should have when they spawn in
     * @param yaw                    the yaw that players should have when they spawn in
     * @param displayName            the display name component for this map
     * @param displayItemSnbt        the SNBT for the item used to represent this map
     * @param introMessages          the messages that may be sent when the game starts
     * @param scoreboardHeader       the component displayed at the top of the scoreboard
     * @param leaderboardPosition    the position of the leaderboard, relative to origin
     * @param leaderboardLength      the number of entries in the leaderboard
     * @param worldTime              the time in ticks that the world should have
     * @param maxPlayers             the maximum number of players this map can fit
     * @param minPlayers             the minimum number of players this map needs to start
     * @param startingCoins          the number of coins each player gets at the start of the game
     * @param repairCoins            the base number of coins each player gets when they repair a window
     * @param windowRepairRadius     the maximum distance away players can be from a window and still repair it
     * @param windowRepairTicks      the number of ticks between each consecutive "repair tick"
     * @param corpseDeathTicks       the number of ticks it takes for a downed player to fully die if they are not revived
     * @param reviveRadius           the maximum distance away players can be from a corpse and still revive it
     * @param canWallshoot           true if wallshooting is enabled, false otherwise
     * @param perksLostOnDeath       true if perks are lost on death, false otherwise
     * @param baseReviveTicks        the base number of ticks it takes to revive a player
     * @param rollsPerChest          the number of rolls a lucky chest can have before it moves to another location
     * @param milestoneRounds        "special" rounds whose times are recorded and saved
     * @param defaultEquipment       the initial equipment players receive when the game starts
     */
    public MapSettingsInfo {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(spawn, "spawn");
        Objects.requireNonNull(displayName, "component");
        Objects.requireNonNull(displayItemSnbt, "displayItemSnbt");
        Objects.requireNonNull(introMessages, "introMessages");
        Objects.requireNonNull(scoreboardHeader, "scoreboardHeader");
        Objects.requireNonNull(leaderboardPosition, "leaderboardPosition");
        Objects.requireNonNull(milestoneRounds, "milestoneRounds");
        Objects.requireNonNull(defaultEquipment, "defaultEquipment");
    }

    /**
     * Constructs a new instance of this record, with default values.
     *
     * @param id     the id of this map
     * @param origin the origin of this map; this is the point from which all other coordinates are relative to unless
     *               otherwise specified
     */
    public MapSettingsInfo(@NotNull Key id, @NotNull Vec3I origin) {
        this(MAP_DATA_VERSION, id, List.of(), origin, 47, -1, Vec3I.ORIGIN, 0, 0, Component.text(id.value()),
                "{id:\"stone\",Count:1,tag:{Name:\"" + id.value() + "\"}}", new ArrayList<>(0),
                Component.text(id.value()), Vec3I.ORIGIN, 15, 0, 4, 1, 0, 20, 3, 1, 20, 500, 20, 2, false, false, 30, 5,
                new ArrayList<>(0), new ArrayList<>(0));
    }
}
