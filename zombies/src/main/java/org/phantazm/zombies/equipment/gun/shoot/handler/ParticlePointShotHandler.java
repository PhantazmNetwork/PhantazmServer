package org.phantazm.zombies.equipment.gun.shoot.handler;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.particle.ParticleCreator;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.particle.ParticleWrapper;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.shoot.GunHit;
import org.phantazm.zombies.equipment.gun.shoot.GunShot;

import java.util.Collection;
import java.util.UUID;

@Model("zombies.gun.shot_handler.particle_point")
@Cache
public class ParticlePointShotHandler implements ShotHandler {
    private final ParticleWrapper wrapper;

    @FactoryMethod
    public ParticlePointShotHandler(@NotNull @Child("particle") ParticleWrapper wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public void handle(@NotNull Gun gun, @NotNull GunState state, @NotNull Entity attacker,
            @NotNull Collection<UUID> previousHits, @NotNull GunShot shot) {
        Instance instance = attacker.getInstance();
        if (instance == null) {
            return;
        }

        for (GunHit hit : shot.regularTargets()) {
            spawnParticle(instance, hit.location());
        }

        for (GunHit hit : shot.headshotTargets()) {
            spawnParticle(instance, hit.location());
        }
    }

    private void spawnParticle(Instance instance, Vec location) {
        ParticleWrapper.Data data = wrapper.data();
        ServerPacket packet =
                ParticleCreator.createParticlePacket(data.particle(), data.distance(), location.x(), location.y(),
                        location.z(), data.offsetX(), data.offsetY(), data.offsetZ(), data.data(), data.particleCount(),
                        wrapper.variantData()::write);
        instance.sendGroupedPacket(packet);
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    @DataObject
    public record Data(@NotNull @ChildPath("particle") String particle) {
    }
}
