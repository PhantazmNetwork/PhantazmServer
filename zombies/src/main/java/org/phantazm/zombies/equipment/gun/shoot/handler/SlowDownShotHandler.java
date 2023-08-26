package org.phantazm.zombies.equipment.gun.shoot.handler;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.ObjectLongPair;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.ExtraNodeKeys;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.shoot.GunHit;
import org.phantazm.zombies.equipment.gun.shoot.GunShot;

import java.util.*;

@Model("zombies.gun.shot_handler.slow_down")
@Cache(false)
public class SlowDownShotHandler implements ShotHandler {

    private static final UUID SLOW_DOWN_UUID = UUID.fromString("aa3a85a6-adca-4268-9940-f34402b1c91d");

    private final Queue<ObjectLongPair<UUID>> removalQueue = new ArrayDeque<>();

    private final Object2LongMap<UUID> latestTimeMap = new Object2LongArrayMap<>();

    private final Data data;

    private long selfTick = 0;

    @FactoryMethod
    public SlowDownShotHandler(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public void tick(@NotNull GunState state, long time) {
        while (!removalQueue.isEmpty()) {
            ObjectLongPair<UUID> toRemove = removalQueue.peek();
            if (toRemove.rightLong() > selfTick) {
                break;
            }

            removalQueue.poll();
            if (toRemove.rightLong() < latestTimeMap.getLong(toRemove.left())) {
                continue;
            }

            latestTimeMap.removeLong(toRemove.left());

            Entity entity = Entity.getEntity(toRemove.left());
            if (!(entity instanceof LivingEntity livingEntity)) {
                continue;
            }

            livingEntity.getAttribute(Attribute.MOVEMENT_SPEED).removeModifier(SLOW_DOWN_UUID);
        }
        ++selfTick;
    }

    @Override
    public void handle(@NotNull Gun gun, @NotNull GunState state, @NotNull Entity attacker,
        @NotNull Collection<UUID> previousHits, @NotNull GunShot shot) {
        for (GunHit target : shot.regularTargets()) {
            if (!(target.entity() instanceof Mob mob)) {
                continue;
            }

            if (mob.data().extra().getBooleanOrDefault(false, ExtraNodeKeys.RESIST_SLOW_DOWN)) {
                continue;
            }

            removalQueue.add(ObjectLongPair.of(target.entity().getUuid(), selfTick + data.headshotDuration()));
            latestTimeMap.put(target.entity().getUuid(), selfTick + data.duration());
            AttributeModifier modifier =
                new AttributeModifier(SLOW_DOWN_UUID, "slowdown_shot_handler", data.multiplier(),
                    AttributeOperation.MULTIPLY_TOTAL);
            AttributeInstance attribute = target.entity().getAttribute(Attribute.MOVEMENT_SPEED);
            attribute.removeModifier(SLOW_DOWN_UUID);
            attribute.addModifier(modifier);
        }
        for (GunHit target : shot.headshotTargets()) {
            if (!(target.entity() instanceof Mob mob)) {
                continue;
            }

            if (mob.data().extra().getBooleanOrDefault(false, ExtraNodeKeys.RESIST_SLOW_DOWN)) {
                continue;
            }

            removalQueue.add(ObjectLongPair.of(target.entity().getUuid(), selfTick + data.headshotDuration()));
            latestTimeMap.put(target.entity().getUuid(), selfTick + data.headshotDuration());
            AttributeModifier modifier =
                new AttributeModifier(SLOW_DOWN_UUID, "slowdown_shot_handler", data.headshotMultiplier(),
                    AttributeOperation.MULTIPLY_TOTAL);
            AttributeInstance attribute = target.entity().getAttribute(Attribute.MOVEMENT_SPEED);
            attribute.removeModifier(SLOW_DOWN_UUID);
            attribute.addModifier(modifier);
        }
    }

    @DataObject
    public record Data(double multiplier,
        double headshotMultiplier,
        long duration,
        long headshotDuration) {

    }

}
