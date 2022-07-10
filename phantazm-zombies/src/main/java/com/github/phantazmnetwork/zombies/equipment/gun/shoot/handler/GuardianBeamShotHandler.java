package com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler;

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
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.monster.GuardianMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.DestroyEntitiesPacket;
import net.minestom.server.network.packet.server.play.SpawnLivingEntityPacket;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

// TODO: verify if we need packets

/**
 * A {@link ShotHandler} which creates a guardian beam.
 */
public class GuardianBeamShotHandler implements ShotHandler {

    /**
     * Data for a {@link GuardianBeamShotHandler}.
     * @param entityType The entity type of the guardian to create a beam with
     * @param beamTime The time in ticks the beam will last
     */
    public record Data(@NotNull EntityType entityType, long beamTime) implements Keyed {

        /**
         * The serial {@link Key} of this {@link Data}.
         */
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.shot_handler.guardian_beam");

        /**
         * Creates a {@link Data}.
         * @param entityType The entity type of the guardian to create a beam with
         * @param beamTime The time in ticks the beam will last
         */
        public Data {
            Objects.requireNonNull(entityType, "entityType");
        }

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                boolean isElder = element.getBooleanOrThrow("isElder");
                long beamTime = element.getNumberOrThrow("beamTime").longValue();
                if (beamTime < 0) {
                    throw new ConfigProcessException("beamTime must be greater than or equal to 0");
                }

                return new Data(isElder ? EntityType.ELDER_GUARDIAN : EntityType.GUARDIAN, beamTime);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) {
                ConfigNode node = new LinkedConfigNode(2);
                node.putBoolean("isElder", data.entityType() == EntityType.ELDER_GUARDIAN);
                node.putNumber("beamTime", data.beamTime());

                return node;
            }
        };
    }

    private record Beam(@NotNull Reference<Instance> instance, @NotNull Entity guardian, @NotNull Entity marker,
                        long time) {

    }

    private final Queue<Beam> removalQueue = new PriorityQueue<>(Comparator.comparingLong(Beam::time));

    private final Data data;

    /**
     * Creates a new {@link GuardianBeamShotHandler}.
     * @param data The data for this {@link GuardianBeamShotHandler}
     */
    public GuardianBeamShotHandler(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public void handle(@NotNull GunState state, @NotNull Entity attacker, @NotNull Collection<UUID> previousHits, @NotNull GunShot shot) {
        Instance instance = attacker.getInstance();
        if (instance == null) {
            return;
        }

        Entity marker = new Entity(EntityType.MARKER);
        EntityMeta markerMeta = marker.getEntityMeta();
        markerMeta.setInvisible(true);

        Entity guardian = new Entity(data.entityType());
        GuardianMeta guardianMeta = (GuardianMeta) guardian.getEntityMeta();
        guardianMeta.setTarget(marker);
        guardian.setInvisible(true);

        Pos start = attacker.getPosition().add(0, attacker.getEyeHeight(), 0);
        ServerPacket markerSpawnPacket = new SpawnLivingEntityPacket(marker.getEntityId(), marker.getUuid(),
                marker.getEntityType().id(), Pos.fromPoint(shot.end()), 0.0F, (short) 0, (short) 0, (short) 0);
        ServerPacket markerMetaPacket = marker.getMetadataPacket();
        ServerPacket guardianSpawnPacket = new SpawnLivingEntityPacket(guardian.getEntityId(), guardian.getUuid(),
                guardian.getEntityType().id(), start, start.yaw(), (short) 0, (short) 0, (short) 0);
        ServerPacket guardianMetaPacket = guardian.getMetadataPacket();

        instance.sendGroupedPacket(markerSpawnPacket);
        instance.sendGroupedPacket(markerMetaPacket);
        instance.sendGroupedPacket(guardianSpawnPacket);
        instance.sendGroupedPacket(guardianMetaPacket);

        removalQueue.add(new Beam(new WeakReference<>(instance), guardian, marker, System.currentTimeMillis()));
    }

    @Override
    public void tick(@NotNull GunState state, long time) {
        for (Beam beam = removalQueue.peek(); beam != null && (time - beam.time()) / 50 > data.beamTime();
             beam = removalQueue.peek()) {
            removalQueue.remove();
            Instance instance = beam.instance().get();
            if (instance != null) {
                instance.sendGroupedPacket(new DestroyEntitiesPacket(List.of(beam.marker().getEntityId(),
                        beam.guardian().getEntityId())));
            }
        }
    }

}