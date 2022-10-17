package com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler;

import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunHit;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunShot;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.damage.DamageType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * A {@link ShotHandler} that deals damage to targets.
 */
@Model("zombies.gun.shot_handler.damage")
@Cache
public class DamageShotHandler implements ShotHandler {

    private final Data data;

    /**
     * Creates a new {@link DamageShotHandler} with the given {@link Data}.
     *
     * @param data The {@link Data} to use
     */
    @FactoryMethod
    public DamageShotHandler(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s
     *
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                float damage = element.getNumberOrThrow("damage").floatValue();
                float headshotDamage = element.getNumberOrThrow("headshotDamage").floatValue();
                if (damage < 0) {
                    throw new ConfigProcessException("damage must be greater than or equal to 0");
                }
                if (headshotDamage < 0) {
                    throw new ConfigProcessException("headshotDamage must be greater than or equal to 0");
                }

                return new Data(damage, headshotDamage);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) {
                ConfigNode node = new LinkedConfigNode(2);
                node.putNumber("damage", data.damage());
                node.putNumber("headshotDamage", data.headshotDamage());
                return node;
            }
        };
    }

    @Override
    public void handle(@NotNull GunState state, @NotNull Entity attacker, @NotNull Collection<UUID> previousHits,
            @NotNull GunShot shot) {
        for (GunHit target : shot.regularTargets()) {
            target.entity().damage(DamageType.fromEntity(attacker), data.damage());
        }
        for (GunHit target : shot.headshotTargets()) {
            target.entity().damage(DamageType.fromEntity(attacker), data.headshotDamage());
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    /**
     * Data for a {@link DamageShotHandler}.
     *
     * @param damage         The amount of damage to deal to regular targets
     * @param headshotDamage The amount of damage to deal to headshots
     */
    @DataObject
    public record Data(float damage, float headshotDamage) {

    }

}
