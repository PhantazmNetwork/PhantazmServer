package com.github.phantazmnetwork.zombies.equipment.gun.shot.handler;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.Gun;
import com.github.phantazmnetwork.zombies.equipment.gun.shot.GunShot;
import net.kyori.adventure.key.Key;
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

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class GuardianBeamShotHandler implements ShotHandler {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.hit_handler.guardian_beam");

    private final Queue<Beam> removalQueue = new ArrayDeque<>();

    private record Beam(@NotNull WeakReference<Instance> instance, @NotNull Entity guardian, @NotNull Entity armorStand,
                        long time) {

    }

    private final EntityType entityType;

    private final long beamTime;

    public GuardianBeamShotHandler(boolean elder, long beamTime) {
        this.entityType = elder ? EntityType.ELDER_GUARDIAN : EntityType.GUARDIAN;
        this.beamTime = beamTime;
    }

    @Override
    public void handle(@NotNull Gun gun, @NotNull Player attacker, @NotNull GunShot shot) {
        Instance instance = attacker.getInstance();
        if (instance == null) {
            return;
        }


        Entity armorStand = new Entity(EntityType.ARMOR_STAND);
        EntityMeta armorStandMeta = armorStand.getEntityMeta();
        armorStandMeta.setInvisible(true);

        Entity guardian = new Entity(entityType);
        GuardianMeta guardianMeta = (GuardianMeta) guardian.getEntityMeta();
        guardianMeta.setTarget(armorStand);
        guardian.setInvisible(true);

        Pos start = attacker.getPosition().add(0, attacker.getEyeHeight(), 0);
        ServerPacket armorStandSpawnPacket = new SpawnLivingEntityPacket(armorStand.getEntityId(), armorStand.getUuid(),
                armorStand.getEntityType().id(), Pos.fromPoint(shot.getEnd()), 0.0F, (short) 0, (short) 0,
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
    public void tick(long time) {
        for (Beam beam = removalQueue.peek(); beam != null && (time - beam.time()) / 50 > beamTime;
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
    public @NotNull Key getSerialKey() {
        return SERIAL_KEY;
    }

}
