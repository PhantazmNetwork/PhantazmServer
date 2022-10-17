package com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler;

import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunShot;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.ExplosionPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * A {@link ShotHandler} which creates an explosion at the end of a shot.
 */
@Model("zombies.gun.shot_handler.explosion")
@Cache
public class ExplosionShotHandler implements ShotHandler {

    private static final byte[] ZERO_BYTE_ARRAY = new byte[0];
    private final Data data;

    /**
     * Creates a new {@link ExplosionShotHandler}.
     *
     * @param data The {@link Data} for this {@link ExplosionShotHandler}
     */
    @FactoryMethod
    public ExplosionShotHandler(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     *
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                float radius = element.getNumberOrThrow("radius").floatValue();
                if (radius < 0) {
                    throw new ConfigProcessException("radius must be greater than or equal to 0");
                }

                return new Data(radius);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) {
                ConfigNode node = new LinkedConfigNode(1);
                node.putNumber("radius", data.radius());
                return node;
            }
        };
    }

    @Override
    public void handle(@NotNull GunState state, @NotNull Entity attacker, @NotNull Collection<UUID> previousHits,
            @NotNull GunShot shot) {
        Instance instance = attacker.getInstance();
        if (instance == null) {
            return;
        }

        Point end = shot.end();
        ServerPacket packet =
                new ExplosionPacket((float)end.x(), (float)end.y(), (float)end.z(), data.radius(), ZERO_BYTE_ARRAY, 0,
                        0, 0);
        instance.sendGroupedPacket(packet);
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    /**
     * Data for an {@link ExplosionShotHandler}.
     *
     * @param radius The radius of the explosion
     */
    @DataObject
    public record Data(float radius) {

    }

}
