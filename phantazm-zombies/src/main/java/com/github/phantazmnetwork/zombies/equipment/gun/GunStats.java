package com.github.phantazmnetwork.zombies.equipment.gun;

public record GunStats(long shootSpeed,
                       long reloadSpeed,
                       int maxAmmo,
                       int maxClip,
                       int shots) {
}
