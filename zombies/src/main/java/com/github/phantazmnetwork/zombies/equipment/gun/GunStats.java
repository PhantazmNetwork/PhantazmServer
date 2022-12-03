package com.github.phantazmnetwork.zombies.equipment.gun;

import com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler.ShotHandler;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A gun's generic stats. These stats only pertain to shooting, not aspects such as damage or knockback.
 * These are handled by individual {@link ShotHandler}s.
 */
@Model("zombies.gun.stats")
@Cache
public final class GunStats {

    private final Data data;


    @FactoryMethod
    public GunStats(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link GunStats}.
     *
     * @return A {@link ConfigProcessor} for {@link GunStats}
     */
    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
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

                return new Data(shootSpeed, reloadSpeed, maxAmmo, maxClip, shots, shotInterval);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) {
                ConfigNode node = new LinkedConfigNode(6);
                node.putNumber("shootSpeed", data.shootSpeed());
                node.putNumber("reloadSpeed", data.reloadSpeed());
                node.putNumber("maxAmmo", data.maxAmmo());
                node.putNumber("maxClip", data.maxClip());
                node.putNumber("shots", data.shots());
                node.putNumber("shotInterval", data.shotInterval());

                return node;
            }
        };
    }

    public long shootSpeed() {
        return data.shootSpeed();
    }

    public long reloadSpeed() {
        return data.reloadSpeed();
    }

    public int maxAmmo() {
        return data.maxAmmo();
    }

    public int maxClip() {
        return data.maxClip();
    }

    public int shots() {
        return data.shots();
    }

    public long shotInterval() {
        return data.shotInterval();
    }

    /**
     * Underlying data for the {@link GunStats}.
     *
     * @param shootSpeed   The gun's shoot speed
     * @param reloadSpeed  The gun's reload speed
     * @param maxAmmo      The gun's max ammo
     * @param maxClip      The gun's max clip
     * @param shots        The gun's shots per clip
     * @param shotInterval The interval between gun fire
     */
    @DataObject
    public record Data(long shootSpeed, long reloadSpeed, int maxAmmo, int maxClip, int shots, long shotInterval) {

    }


}
