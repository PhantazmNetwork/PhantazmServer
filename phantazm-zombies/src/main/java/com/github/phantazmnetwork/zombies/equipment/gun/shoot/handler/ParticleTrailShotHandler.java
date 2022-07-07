package com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler;

import com.github.phantazmnetwork.api.particle.ParticleWrapper;
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
import net.minestom.server.particle.ParticleCreator;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public class ParticleTrailShotHandler implements ShotHandler {

    public record Data(@NotNull ParticleWrapper particle, int trailCount) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.shot_handler.particle_trail");

        public Data {
            Objects.requireNonNull(particle, "particle");
        }

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor(@NotNull ConfigProcessor<ParticleWrapper> particleProcessor) {

        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                ParticleWrapper particle = particleProcessor.dataFromElement(element.getElementOrThrow("particle"));
                int trailCount = element.getNumberOrThrow("trailCount").intValue();
                if (trailCount < 0) {
                    throw new ConfigProcessException("trailCount must be greater than or equal to 0");
                }

                return new Data(particle, trailCount);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(2);
                node.put("particle", particleProcessor.elementFromData(data.particle()));
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

        ParticleWrapper particle = data.particle();
        Pos start = shot.start();
        Vec direction = Vec.fromPoint(shot.end().sub(start)).normalize();
        for (int i = 0; i < data.trailCount(); i++) {
            start = start.add(direction);

            ServerPacket packet = ParticleCreator.createParticlePacket(particle.particle(), particle.distance(),
                    start.x(), start.y(), start.z(), particle.offsetX(), particle.offsetY(), particle.offsetZ(),
                    particle.particleData(), particle.particleCount(), particle.data()::write);
            instance.sendGroupedPacket(packet);
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

}
