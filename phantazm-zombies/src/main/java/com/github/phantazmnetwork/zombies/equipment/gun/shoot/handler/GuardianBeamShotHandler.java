package com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.PhantazmMob;
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
import net.minestom.server.entity.Player;
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
public class GuardianBeamShotHandler implements ShotHandler {

    public record Data(@NotNull EntityType entityType, long beamTime) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.shot_handler.guardian_beam");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                boolean isElder = element.getBooleanOrThrow("isElder");
                long beamTime = element.getNumberOrThrow("beamTime").longValue();

                return new Data(isElder ? EntityType.ELDER_GUARDIAN : EntityType.GUARDIAN, beamTime);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(1);
                node.putBoolean("isElder", data.entityType() == EntityType.ELDER_GUARDIAN);
                node.putNumber("beamTime", data.beamTime());
                return node;
            }
        };
    }

    private record Beam(@NotNull Reference<Instance> instance, @NotNull Entity guardian, @NotNull Entity armorStand,
                        long time) {

    }

    private final Queue<Beam> removalQueue = new PriorityQueue<>(Comparator.comparingLong(Beam::time));

    private final Data data;

    public GuardianBeamShotHandler(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public void handle(@NotNull GunState state, @NotNull Player attacker, @NotNull Collection<PhantazmMob> previousHits, @NotNull GunShot shot) {
        Instance instance = attacker.getInstance();
        if (instance == null) {
            return;
        }

        Entity armorStand = new Entity(EntityType.ARMOR_STAND);
        EntityMeta armorStandMeta = armorStand.getEntityMeta();
        armorStandMeta.setInvisible(true);

        Entity guardian = new Entity(data.entityType());
        GuardianMeta guardianMeta = (GuardianMeta) guardian.getEntityMeta();
        guardianMeta.setTarget(armorStand);
        guardian.setInvisible(true);

        Pos start = attacker.getPosition().add(0, attacker.getEyeHeight(), 0);
        ServerPacket armorStandSpawnPacket = new SpawnLivingEntityPacket(armorStand.getEntityId(), armorStand.getUuid(),
                armorStand.getEntityType().id(), Pos.fromPoint(shot.end()), 0.0F, (short) 0, (short) 0,
                (short) 0);
        ServerPacket armorStandMetaPacket = armorStand.getMetadataPacket();
        ServerPacket guardianSpawnPacket = new SpawnLivingEntityPacket(guardian.getEntityId(), guardian.getUuid(),
                guardian.getEntityType().id(), start, start.yaw(), (short) 0, (short) 0, (short) 0);
        ServerPacket guardianMetaPacket = guardian.getMetadataPacket();

        instance.sendGroupedPacket(armorStandSpawnPacket);
        instance.sendGroupedPacket(armorStandMetaPacket);
        instance.sendGroupedPacket(guardianSpawnPacket);
        instance.sendGroupedPacket(guardianMetaPacket);

        removalQueue.add(new Beam(new WeakReference<>(instance), guardian, armorStand, System.currentTimeMillis()));
    }

    @Override
    public void tick(@NotNull GunState state, long time) {
        for (Beam beam = removalQueue.peek(); beam != null && (time - beam.time()) / 50 > data.beamTime();
             beam = removalQueue.peek()) {
            removalQueue.remove();
            Instance instance = beam.instance().get();
            if (instance != null) {
                instance.sendGroupedPacket(new DestroyEntitiesPacket(List.of(beam.armorStand().getEntityId(),
                        beam.guardian().getEntityId())));
            }
        }
    }

    @Override
    public @NotNull Keyed getData() {
        return data;
    }

}
