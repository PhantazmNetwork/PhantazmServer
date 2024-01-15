package org.phantazm.zombies.map;

import com.github.steanky.ethylene.core.collection.ConfigNode;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Represents a Zombies map.
 */
public record MapInfo(
    @NotNull MapSettingsInfo settings,
    @NotNull PlayerCoinsInfo playerCoins,
    @NotNull List<RoomInfo> rooms,
    @NotNull List<DoorInfo> doors,
    @NotNull List<ShopInfo> shops,
    @NotNull List<WindowInfo> windows,
    @NotNull List<RoundInfo> rounds,
    @NotNull List<SpawnruleInfo> spawnrules,
    @NotNull List<SpawnpointInfo> spawnpoints,
    @NotNull ConfigNode leaderboard,
    @NotNull ConfigNode scoreboard,
    @NotNull ConfigNode corpse,
    @NotNull ConfigNode endless,
    @NotNull WebhookInfo webhook) implements Keyed {
    /**
     * Constructs a new instances of this record.
     *
     * @param settings    the settings defining the general parameters for this map
     * @param rooms       this map's rooms
     * @param doors       this map's doors
     * @param shops       this map's shops
     * @param windows     this map's windows
     * @param rounds      this map's rounds
     * @param spawnrules  this map's spawnrules
     * @param spawnpoints this map's spawnpoints
     * @param scoreboard  this map's scoreboard info
     */
    public MapInfo(@NotNull MapSettingsInfo settings, @NotNull PlayerCoinsInfo playerCoins,
        @NotNull List<RoomInfo> rooms, @NotNull List<DoorInfo> doors, @NotNull List<ShopInfo> shops,
        @NotNull List<WindowInfo> windows, @NotNull List<RoundInfo> rounds, @NotNull List<SpawnruleInfo> spawnrules,
        @NotNull List<SpawnpointInfo> spawnpoints, @NotNull ConfigNode leaderboard,
        @NotNull ConfigNode scoreboard, @NotNull ConfigNode corpse, @NotNull ConfigNode endless,
        @NotNull WebhookInfo webhook) {
        rounds.sort(Comparator.comparingInt(RoundInfo::round));
        
        this.settings = Objects.requireNonNull(settings);
        this.playerCoins = Objects.requireNonNull(playerCoins);
        this.rooms = Objects.requireNonNull(rooms);
        this.doors = Objects.requireNonNull(doors);
        this.shops = Objects.requireNonNull(shops);
        this.windows = Objects.requireNonNull(windows);
        this.rounds = Objects.requireNonNull(rounds);
        this.spawnrules = Objects.requireNonNull(spawnrules);
        this.spawnpoints = Objects.requireNonNull(spawnpoints);
        this.leaderboard = Objects.requireNonNull(leaderboard);
        this.scoreboard = Objects.requireNonNull(scoreboard);
        this.corpse = Objects.requireNonNull(corpse);
        this.endless = Objects.requireNonNull(endless);
        this.webhook = Objects.requireNonNull(webhook);
    }

    @Override
    public @NotNull Key key() {
        return settings.id();
    }
}
