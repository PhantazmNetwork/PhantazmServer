package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ParticleTrailEffect implements GunEffect {

    public record Data(@NotNull Particle particle,
                                          boolean distance,
                                          float offsetX,
                                          float offsetY,
                                          float offsetZ,
                                          float particleData,
                                          int count,
                                          int trailCount) implements VariantSerializable {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.effect.particle_trail");

        public Data {
            Objects.requireNonNull(particle, "particle");
        }

        @Override
        public @NotNull Key getSerialKey() {
            return SERIAL_KEY;
        }
    }

    private final Data data;

    private final PlayerView playerView;

    public ParticleTrailEffect(@NotNull Data data, @NotNull PlayerView playerView) {
        this.data = Objects.requireNonNull(data, "data");
        this.playerView = playerView;
    }

    @Override
    public void accept(@NotNull GunState state) {
        playerView.getPlayer().ifPresent(player -> {
            Instance instance = player.getInstance();
            if (instance == null) {
                return;
            }

            Pos position = player.getPosition().add(0, player.getEyeHeight(), 0);
            Vec direction = position.direction();
            for (int i = 0; i < data.trailCount(); i++) {
                position = position.add(direction);

                ServerPacket packet = ParticleCreator.createParticlePacket(data.particle(), data.distance(),
                        position.x(), position.y(), position.z(), data.offsetX(), data.offsetY(), data.offsetZ(),
                        data.particleData(), data.count(), null);
                instance.sendGroupedPacket(packet);
            }
        });
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    @Override
    public @NotNull VariantSerializable getData() {
        return data;
    }

}
