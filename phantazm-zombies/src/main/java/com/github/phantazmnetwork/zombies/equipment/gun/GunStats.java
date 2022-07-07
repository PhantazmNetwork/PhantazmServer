package com.github.phantazmnetwork.zombies.equipment.gun;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

public record GunStats(long shootSpeed,
                       long reloadSpeed,
                       int maxAmmo,
                       int maxClip,
                       int shots,
                       long shotInterval) implements Keyed {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.stats");

    public static @NotNull ConfigProcessor<GunStats> processor() {
        return new ConfigProcessor<>() {

            @Override
            public @NotNull GunStats dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                long shootSpeed = element.getNumberOrThrow("shootSpeed").longValue();
                if (shootSpeed < 0) {
                    throw new ConfigProcessException("shootSpeed must be greater than or equal to 0");
                }
                long reloadSpeed = element.getNumberOrThrow("reloadSpeed").longValue();
                if (reloadSpeed < 0) {
                    throw new ConfigProcessException("reloadSpeed must be greater than or equal to 0");
                }
                int maxAmmo = element.getNumberOrThrow("maxAmmo").intValue();
                if (maxAmmo < 0) {
                    throw new ConfigProcessException("maxAmmo must be greater than or equal to 0");
                }
                int maxClip = element.getNumberOrThrow("maxClip").intValue();
                if (maxClip < 0) {
                    throw new ConfigProcessException("maxClip must be greater than or equal to 0");
                }
                int shots = element.getNumberOrThrow("shots").intValue();
                if (shots < 0) {
                    throw new ConfigProcessException("shots must be greater than or equal to 0");
                }
                long shotInterval = element.getNumberOrThrow("shotInterval").longValue();
                if (shotInterval < 0) {
                    throw new ConfigProcessException("shotInterval must be greater than or equal to 0");
                }

                return new GunStats(shootSpeed, reloadSpeed, maxAmmo, maxClip, shots, shotInterval);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull GunStats stats) {
                ConfigNode node = new LinkedConfigNode(6);
                node.putNumber("shootSpeed", stats.shootSpeed());
                node.putNumber("reloadSpeed", stats.reloadSpeed());
                node.putNumber("maxAmmo", stats.maxAmmo());
                node.putNumber("maxClip", stats.maxClip());
                node.putNumber("shots", stats.shots());
                node.putNumber("shotInterval", stats.shotInterval());

                return node;
            }
        };
    }

    public GunStats {
        if (shootSpeed < 0) {
            throw new IllegalArgumentException("shootSpeed must be greater than or equal to 0");
        }
        if (reloadSpeed < 0) {
            throw new IllegalArgumentException("reloadSpeed must be greater than or equal to 0");
        }
        if (maxAmmo < 0) {
            throw new IllegalArgumentException("maxAmmo must be greater than or equal to 0");
        }
        if (maxClip < 0) {
            throw new IllegalArgumentException("maxClip must be greater than or equal to 0");
        }
        if (shots < 0) {
            throw new IllegalArgumentException("shots must be greater than or equal to 0");
        }
        if (shotInterval < 0) {
            throw new IllegalArgumentException("shotInterval must be greater than or equal to 0");
        }
    }

    @Override
    public @NotNull Key key() {
        return SERIAL_KEY;
    }
}
