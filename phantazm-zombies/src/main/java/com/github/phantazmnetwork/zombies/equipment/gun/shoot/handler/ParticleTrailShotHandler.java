package com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler;

import com.github.phantazmnetwork.api.config.processor.MinestomConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunShot;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public class ParticleTrailShotHandler implements ShotHandler {

    public record Data(@NotNull Particle particle,
                       boolean distance,
                       float offsetX,
                       float offsetY,
                       float offsetZ,
                       float particleData,
                       int count,
                       int trailCount) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.shot_handler.particle_trail");

        public Data {
            Objects.requireNonNull(particle, "particle");
        }

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<Particle> particleProcessor = MinestomConfigProcessors.particle();
        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Particle particle = particleProcessor.dataFromElement(element.getElementOrThrow("particle"));
                boolean distance = element.getBooleanOrThrow("distance");
                float offsetX = element.getNumberOrThrow("offsetX").floatValue();
                float offsetY = element.getNumberOrThrow("offsetY").floatValue();
                float offsetZ = element.getNumberOrThrow("offsetZ").floatValue();
                float particleData = element.getNumberOrThrow("particleData").floatValue();
                int count = element.getNumberOrThrow("count").intValue();
                int trailCount = element.getNumberOrThrow("trailCount").intValue();

                return new Data(particle, distance, offsetX, offsetY, offsetZ, particleData, count, trailCount);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(8);
                node.put("particle", particleProcessor.elementFromData(data.particle()));
                node.putBoolean("distance", data.distance());
                node.putNumber("offsetX", data.offsetX());
                node.putNumber("offsetY", data.offsetY());
                node.putNumber("offsetZ", data.offsetZ());
                node.putNumber("particleData", data.particleData());
                node.putNumber("count", data.count());
                node.putNumber("trailCount", data.trailCount());

                return node;
            }
        };
    }

    private final Data data;

    public ParticleTrailShotHandler(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public void handle(@NotNull GunState state, @NotNull Entity attacker, @NotNull Collection<UUID> previousHits,
                       @NotNull GunShot shot) {
        Instance instance = attacker.getInstance();
        if (instance == null) {
            return;
        }

        Pos start = shot.start();
        Vec direction = Vec.fromPoint(shot.end().sub(start)).normalize();
        for (int i = 0; i < data.trailCount(); i++) {
            start = start.add(direction);

            ServerPacket packet = ParticleCreator.createParticlePacket(data.particle(), data.distance(),
                    start.x(), start.y(), start.z(), data.offsetX(), data.offsetY(), data.offsetZ(),
                    data.particleData(), data.count(), null);
            instance.sendGroupedPacket(packet);
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

}
