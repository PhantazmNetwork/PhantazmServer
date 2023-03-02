package org.phantazm.zombies.equipment.perk.effect.shot;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.annotation.document.Description;
import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Tickable;
import org.phantazm.zombies.map.action.Action;

import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Description("""
        An entity action that sets an entity on fire for a configurable amount of time, health, and damage interval.
        """)
@Model("zombies.perk.effect.shot_entity.apply_fire")
@Cache(false)
public class ApplyFireShotEffect implements Action<Entity>, Tickable {
    private static final Map<Data, Pair<String, String>> NAMES = new ConcurrentHashMap<>();

    private final Data data;

    private final Tag<Long> timeTag;
    private final Tag<Long> timeSinceLastDamage;

    private final Deque<LivingEntity> activeEntities;

    @FactoryMethod
    public ApplyFireShotEffect(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");

        Pair<String, String> stringPair =
                NAMES.computeIfAbsent(data, key -> Pair.of(UUID.randomUUID().toString(), UUID.randomUUID().toString()));

        this.timeTag = Tag.Long(stringPair.left()).defaultValue(-1L);
        this.timeSinceLastDamage = Tag.Long(stringPair.right()).defaultValue(-1L);

        this.activeEntities = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void perform(@NotNull Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            //can't set non-LivingEntity on fire as they have no health
            return;
        }

        long tag = entity.getTag(timeTag);
        entity.setTag(timeTag, System.currentTimeMillis());

        if (tag == -1) {
            activeEntities.add(livingEntity);
        }
    }

    @Override
    public void tick(long time) {
        activeEntities.removeIf(entity -> process(entity, time));
    }

    private boolean process(LivingEntity entity, long time) {
        if (entity.isRemoved() || entity.isDead()) {
            stopFire(entity);
            return true;
        }

        long lastApply = entity.getTag(timeTag);
        if (lastApply == -1 || (time - lastApply) / MinecraftServer.TICK_MS >= data.fireTicks) {
            //fire has expired, or never should have been applied in the first place
            stopFire(entity);
            return true;
        }

        long lastDamageTime = entity.getTag(timeSinceLastDamage);
        if (lastDamageTime == -1 || (time - lastDamageTime) / MinecraftServer.TICK_MS >= data.damageInterval) {
            entity.setOnFire(true);
            doDamage(entity);
            entity.setTag(timeSinceLastDamage, time);
        }

        return false;
    }

    private void doDamage(LivingEntity entity) {
        entity.damage(DamageType.ON_FIRE, data.damage);
    }

    private void stopFire(Entity entity) {
        entity.setOnFire(false);
        entity.removeTag(timeTag);
        entity.removeTag(timeSinceLastDamage);
    }

    @DataObject
    public record Data(@Description("The number of ticks the hit entity will be set on fire") int fireTicks,
                       @Description("The number of ticks between fire damage applications") int damageInterval,
                       @Description("The amount of damage dealt on each application") float damage) {
    }
}
