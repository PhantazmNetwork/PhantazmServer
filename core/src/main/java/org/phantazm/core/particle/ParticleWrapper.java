package org.phantazm.core.particle;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.coordinate.Point;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.particle.data.ParticleVariantData;

@Model("particle.wrapper")
@Cache
public class ParticleWrapper {
    private final Data data;
    private final ParticleVariantData particleVariantData;

    @FactoryMethod
    public ParticleWrapper(@NotNull Data data,
        @NotNull @Child("variantData") ParticleVariantData particleVariantData) {
        this.data = data;
        this.particleVariantData = particleVariantData;
    }

    public @NotNull Data data() {
        return data;
    }

    public @NotNull ParticleVariantData variantData() {
        return particleVariantData;
    }

    public void sendTo(@NotNull PacketGroupingAudience audience, double x, double y, double z) {
        ServerPacket packet =
            ParticleCreator.createParticlePacket(data.particle, data.distance, x, y, z, data.offsetX, data.offsetY,
                data.offsetZ, data.data, data.particleCount, particleVariantData::write);
        audience.sendGroupedPacket(packet);
    }

    public void sendTo(@NotNull PacketGroupingAudience audience, @NotNull Point point) {
        sendTo(audience, point.x(), point.y(), point.z());
    }

    @DataObject
    public record Data(
        @NotNull Particle particle,
        boolean distance,
        float offsetX,
        float offsetY,
        float offsetZ,
        float data,
        int particleCount) {
    }
}
