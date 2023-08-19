package org.phantazm.zombies.map;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

/**
 * Defines a spawnrule, which acts as a whitelist/blacklist filter for determining whether a mob may spawn at a
 * spawnpoint.
 */
public record SpawnruleInfo(@NotNull Key id,
    @NotNull Key spawnType,
    @NotNull Set<Key> spawns,
    boolean isBlacklist,
    int sla) {
    /**
     * Creates a new instance of this record.
     *
     * @param id          the id of this spawnrule
     * @param spawnType   the type of this spawnrule, which may be used as a broad filter for certain types of mobs
     * @param spawns      the explicit mob type filter list (may be a whitelist or blacklist)
     * @param isBlacklist whether this spawnrule is considered a blacklist or whitelist
     */
    public SpawnruleInfo {
        Objects.requireNonNull(id);
        Objects.requireNonNull(spawnType);
        Objects.requireNonNull(spawns);
    }

    /**
     * Returns the squared SLA.
     *
     * @return the squared sla, or {@code sla * sla}
     */
    public double slaSquared() {
        return sla * sla;
    }
}
