package org.phantazm.zombies.equipment.gun.shoot.handler;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.monster.GuardianMeta;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.DestroyEntitiesPacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.shoot.GunShot;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

// TODO: verify if we need packets

/**
 * A {@link ShotHandler} which creates a guardian beam.
 */
@Model("zombies.gun.shot_handler.guardian_beam")
@Cache(false)
public class GuardianBeamShotHandler implements ShotHandler {

    private final Queue<Beam> removalQueue = new PriorityQueue<>(Comparator.comparingLong(Beam::ticks));
    private final Data data;
    private long ticks = 0;

    /**
     * Creates a new {@link GuardianBeamShotHandler}.
     *
     * @param data The data for this {@link GuardianBeamShotHandler}
     */
    @FactoryMethod
    public GuardianBeamShotHandler(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
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
                boolean isElder = element.atOrThrow("isElder").asBooleanOrThrow();
                long beamTime = element.atOrThrow("beamTime").asNumberOrThrow().longValue();
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

    @Override
    public void handle(@NotNull Gun gun, @NotNull GunState state, @NotNull Entity attacker,
        @NotNull Collection<UUID> previousHits, @NotNull GunShot shot) {
        Instance instance = attacker.getInstance();
        if (instance == null) {
            return;
        }

        Entity armorStand = new Entity(EntityType.ARMOR_STAND);
        ArmorStandMeta armorStandMeta = (ArmorStandMeta) armorStand.getEntityMeta();
        armorStandMeta.setMarker(true);
        armorStandMeta.setInvisible(true);

        Entity guardian = new Entity(data.entityType());
        GuardianMeta guardianMeta = (GuardianMeta) guardian.getEntityMeta();
        guardianMeta.setTarget(armorStand);
        guardian.setInvisible(true);

        Pos start = attacker.getPosition().add(0, attacker.getEyeHeight(), 0);
        ServerPacket armorStandSpawnPacket =
            new SpawnEntityPacket(armorStand.getEntityId(), armorStand.getUuid(), armorStand.getEntityType().id(),
                Pos.fromPoint(shot.end()), 0F, 0, (short) 0, (short) 0, (short) 0);
        ServerPacket armorStandMetaPacket = armorStand.getMetadataPacket();
        ServerPacket guardianSpawnPacket =
            new SpawnEntityPacket(guardian.getEntityId(), guardian.getUuid(), guardian.getEntityType().id(), start,
                start.yaw(), 0, (short) 0, (short) 0, (short) 0);
        ServerPacket guardianMetaPacket = guardian.getMetadataPacket();

        instance.sendGroupedPacket(armorStandSpawnPacket);
        instance.sendGroupedPacket(armorStandMetaPacket);
        instance.sendGroupedPacket(guardianSpawnPacket);
        instance.sendGroupedPacket(guardianMetaPacket);

        removalQueue.add(new Beam(new WeakReference<>(instance), guardian, armorStand, ticks));
    }

    @Override
    public void tick(@NotNull GunState state, long time) {
        ++ticks;
        for (Beam beam = removalQueue.peek(); beam != null && ticks - beam.ticks() > data.beamTime();
             beam = removalQueue.peek()) {
            removalQueue.remove();
            Instance instance = beam.instance().get();
            if (instance != null) {
                instance.sendGroupedPacket(new DestroyEntitiesPacket(
                    List.of(beam.armorStand().getEntityId(), beam.guardian().getEntityId())));
            }
        }
    }

    /**
     * Data for a {@link GuardianBeamShotHandler}.
     *
     * @param entityType The entity type of the guardian to create a beam with
     * @param beamTime   The time in ticks the beam will last
     */
    @DataObject
    public record Data(@NotNull EntityType entityType,
        long beamTime) {
    }

    private record Beam(
        @NotNull Reference<Instance> instance,
        @NotNull Entity guardian,
        @NotNull Entity armorStand,
        long ticks) {

    }

}
