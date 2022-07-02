package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.Gun;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import org.jetbrains.annotations.NotNull;

public class ParticleTrailEffect implements GunEffect {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.effect.particle_trail");

    private final Particle particle;

    private final boolean distance;

    private final float offsetX;

    private final float offsetY;

    private final float offsetZ;

    private final float particleData;

    private final int count;

    private final int trailCount;

    public ParticleTrailEffect(@NotNull Particle particle, boolean distance,
                               float offsetX, float offsetY, float offsetZ,
                               float particleData, int count, int trailCount) {
        this.particle = particle;
        this.distance = distance;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.particleData = particleData;
        this.count = count;
        this.trailCount = trailCount;
    }

    @Override
    public void accept(@NotNull Gun gun) {
        gun.getOwner().getPlayer().ifPresent(player -> {
            Instance instance = player.getInstance();
            if (instance == null) {
                return;
            }

            Pos position = player.getPosition().add(0, player.getEyeHeight(), 0);
            Vec direction = position.direction();
            for (int i = 0; i < trailCount; i++) {
                position = position.add(direction);

                ServerPacket packet = ParticleCreator.createParticlePacket(particle, distance, position.x(),
                        position.y(), position.z(), offsetX, offsetY, offsetZ, particleData, count, null);
                instance.sendGroupedPacket(packet);
            }
        });
    }

    @Override
    public @NotNull Key getSerialKey() {
        return SERIAL_KEY;
    }
}
