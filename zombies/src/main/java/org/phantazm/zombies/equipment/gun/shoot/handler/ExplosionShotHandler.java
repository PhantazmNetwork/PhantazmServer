package org.phantazm.zombies.equipment.gun.shoot.handler;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.ExplosionPacket;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.shoot.GunShot;

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

    @Override
    public void handle(@NotNull Gun gun, @NotNull GunState state, @NotNull Entity attacker,
            @NotNull Collection<UUID> previousHits, @NotNull GunShot shot) {
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
