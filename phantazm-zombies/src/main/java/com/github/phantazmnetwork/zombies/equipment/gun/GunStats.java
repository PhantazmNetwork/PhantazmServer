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
                long reloadSpeed = element.getNumberOrThrow("reloadSpeed").longValue();
                int maxAmmo = element.getNumberOrThrow("maxAmmo").intValue();
                int maxClip = element.getNumberOrThrow("maxClip").intValue();
                int shots = element.getNumberOrThrow("shots").intValue();
                long shotInterval = element.getNumberOrThrow("shotInterval").longValue();

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

    @Override
    public @NotNull Key key() {
        return SERIAL_KEY;
    }
}
