package com.github.phantazmnetwork.zombies.map;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Defines a spawnrule, which acts as a whitelist/blacklist filter for determining whether a mob may spawn at a
 * spawnpoint.
 */
public record SpawnruleInfo(@NotNull Key id,
                            @NotNull Key spawnType,
                            @NotNull List<Key> spawns,
                            boolean isBlacklist) {
    /**
     * Creates a new instance of this record.
     * @param id the id of this spawnrule
     * @param spawnType the type of this spawnrule, which may be used as a broad filter for certain types of mobs
     * @param spawns the explicit mob type filter list (may be a whitelist or blacklist)
     * @param isBlacklist whether this spawnrule is considered a blacklist or whitelist
     */
    public SpawnruleInfo {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(spawnType, "spawnType");
        Objects.requireNonNull(spawns, "spawns");
    }
}
