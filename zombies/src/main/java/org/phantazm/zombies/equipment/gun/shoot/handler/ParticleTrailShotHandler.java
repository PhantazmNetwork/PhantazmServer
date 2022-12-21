package org.phantazm.zombies.equipment.gun.shoot.handler;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.particle.ParticleCreator;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.config.processor.ItemStackConfigProcessors;
import org.phantazm.core.particle.ParticleWrapper;
import org.phantazm.core.particle.data.ParticleData;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.shoot.GunShot;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * A {@link ShotHandler} that creates a trail of particles.
 */
@Model("zombies.gun.shot_handler.particle_trail")
@Cache
public class ParticleTrailShotHandler implements ShotHandler {

    private final Data data;

    /**
     * Creates a {@link ParticleTrailShotHandler}.
     *
     * @param data The {@link ParticleTrailShotHandler}'s {@link Data}
     */
    @FactoryMethod
    public ParticleTrailShotHandler(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     *
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<ParticleWrapper> particleProcessor =
                ParticleWrapper.processor(ParticleData.processor(ItemStackConfigProcessors.snbt()));

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

            ServerPacket packet =
                    ParticleCreator.createParticlePacket(particle.particle(), particle.distance(), start.x(), start.y(),
                            start.z(), particle.offsetX(), particle.offsetY(), particle.offsetZ(),
                            particle.particleData(), particle.particleCount(), particle.data()::write);
            instance.sendGroupedPacket(packet);
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    /**
     * Data for a {@link ParticleTrailShotHandler}.
     *
     * @param particle   The {@link ParticleWrapper} to use for the trail
     * @param trailCount The number of particles to create in the trail
     */
    @DataObject
    public record Data(@NotNull ParticleWrapper particle, int trailCount) {

        /**
         * Creates a {@link Data}.
         *
         * @param particle   The {@link ParticleWrapper} to use for the trail
         * @param trailCount The number of particles to create in the trail
         */
        public Data {
            Objects.requireNonNull(particle, "particle");
        }

    }

}
