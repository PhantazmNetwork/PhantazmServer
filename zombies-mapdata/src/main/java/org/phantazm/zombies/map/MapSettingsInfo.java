package org.phantazm.zombies.map;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.mapper.annotation.Default;
import com.github.steanky.vector.Vec3I;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.chat.MessageWithDestination;

import java.util.*;

/**
 * Defines the general settings for a map.
 */
public record MapSettingsInfo(int mapDataVersion,
                              int chunkLoadRange,
                              @NotNull Key id,
                              @NotNull List<String> instancePath,
                              @NotNull Vec3I origin,
                              double coinsLostOnKnock,
                              @NotNull List<String> requiredPermissions,
                              int minimumProtocolVersion,
                              int maximumProtocolVersion,
                              @NotNull Vec3I spawn,
                              float pitch,
                              float yaw,
                              @NotNull Component displayName,
                              @NotNull String displayItemSnbt,
                              long idleRevertTicks,
                              @NotNull List<List<MessageWithDestination>> introMessages,
                              long countdownTicks,
                              @NotNull List<Long> countdownAlertTicks,
                              @NotNull Sound countdownTickSound,
                              @NotNull String countdownTimeFormat,
                              long endTicks,
                              @NotNull String endGameStatsFormat,
                              @NotNull Component scoreboardHeader,
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
                              @NotNull List<Key> lostOnDeath,
                              long baseReviveTicks,
                              int rollsPerChest,
                              float punchDamage,
                              float punchRange,
                              float punchKnockback,
                              int punchCooldown,
                              boolean mobPlayerCollisions,
                              @NotNull Map<Key, List<Key>> defaultEquipment,
                              @NotNull Map<Key, EquipmentGroupInfo> equipmentGroups,
                              @NotNull String winTitleFormat,
                              @NotNull String winSubtitleFormat,
                              @NotNull String lossTitleFormat,
                              @NotNull String lossSubtitleFormat,
                              @NotNull String reviveStatusToReviverFormat,
                              @NotNull String reviveStatusToKnockedFormat,
                              @NotNull String dyingStatusFormat,
                              @NotNull String reviveMessageToRevivedFormat,
                              @NotNull String reviveMessageToOthersFormat,
                              @NotNull Sound reviveSound,
                              @NotNull String knockedMessageToKnockedFormat,
                              @NotNull String knockedMessageToOthersFormat,
                              @NotNull String knockedTitleFormat,
                              @NotNull String knockedSubtitleFormat,
                              @NotNull Sound knockedSound,
                              @NotNull String deathMessageToKilledFormat,
                              @NotNull String deathMessageToOthersFormat,
                              @NotNull Sound deathSound,
                              @NotNull String rejoinMessageFormat,
                              @NotNull String quitMessageFormat,
                              @NotNull Component nearWindowMessage,
                              @NotNull Component startRepairingMessage,
                              @NotNull Component stopRepairingMessage,
                              @NotNull Component finishRepairingMessage,
                              @NotNull Component enemiesNearbyMessage,
                              @NotNull Component healthDisplay,
                              @NotNull String gameJoinFormat) {

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
     * @param worldTime              the time in ticks that the world should have
     * @param maxPlayers             the maximum number of players this map can fit
     * @param minPlayers             the minimum number of players this map needs to start
     * @param startingCoins          the number of coins each player gets at the start of the game
     * @param repairCoins            the base number of coins each player gets when they repair a window
     * @param windowRepairRadius     the maximum distance away players can be from a window and still repair it
     * @param windowRepairTicks      the number of ticks between each consecutive "repair tick"
     * @param corpseDeathTicks       the number of ticks it takes for a downed player to fully die if they are not revived
     * @param reviveRadius           the maximum distance away players can be from a corpse and still revive it
     * @param lostOnDeath            object groups that are deleted on death
     * @param baseReviveTicks        the base number of ticks it takes to revive a player
     * @param rollsPerChest          the number of rolls a lucky chest can have before it moves to another location
     * @param defaultEquipment       the initial equipment players receive when the game starts; the keys correspond to
     *                               the inventory object group they should be placed in
     */
    public MapSettingsInfo {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(spawn, "spawn");
        Objects.requireNonNull(displayName, "component");
        Objects.requireNonNull(displayItemSnbt, "displayItemSnbt");
        Objects.requireNonNull(introMessages, "introMessages");
        Objects.requireNonNull(scoreboardHeader, "scoreboardHeader");
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
        this(MAP_DATA_VERSION, 10, id, List.of(), origin, -1, List.of(), 47, -1, Vec3I.ORIGIN, 0, 0,
                Component.text(id.value()), "{id:\"stone\",Count:1,tag:{Name:\"" + id.value() + "\"}}", 12000L,
                new ArrayList<>(0), 400L, new ArrayList<>(0),
                Sound.sound(Key.key("minecraft:entity.wolf.howl"), Sound.Source.MASTER, 1.0F, 1.0F), "", 200L, "",
                Component.text(id.value()), 0, 4, 1, 0, 20, 3, 1, 20, 500, 20, 2, false, new ArrayList<>(), 30, 5, 0,
                4.5F, 0.4F, 20, false, new HashMap<>(0), new HashMap<>(), "", "", "", "", "", "", "", "", "",
                Sound.sound(Key.key("minecraft:block.brewing_stand.brew"), Sound.Source.MASTER, 1.0F, 1.0F), "", "", "",
                "", Sound.sound(Key.key("minecraft:entity.ender_dragon.growl"), Sound.Source.MASTER, 1.0F, 0.5F), "",
                "", Sound.sound(Key.key("minecraft:entity.player.hurt"), Sound.Source.MASTER, 1.0F, 1.0F), "", "",
                Component.text("Hold SNEAK to repair", NamedTextColor.GREEN),
                Component.text("Started repairing. Keep holding SNEAK to continue.", NamedTextColor.GREEN),
                Component.text("Stopped repairing.", NamedTextColor.RED),
                Component.text("Fully repaired!", NamedTextColor.GREEN),
                Component.text("You cannot repair that window while enemies are nearby!", NamedTextColor.RED),
                Component.text("❤", NamedTextColor.RED), "");
    }

    @Default("chunkLoadRange")
    public static @NotNull ConfigElement defaultChunkLoadRange() {
        return ConfigPrimitive.of(10);
    }

    @Default("requiredPermissions")
    public static @NotNull ConfigElement defaultRequiredPermissions() {
        return ConfigList.of();
    }

    @Default("coinsLostOnKnock")
    public static @NotNull ConfigElement defaultCoinsLostOnKnock() {
        return ConfigPrimitive.of(0.25);
    }
}
