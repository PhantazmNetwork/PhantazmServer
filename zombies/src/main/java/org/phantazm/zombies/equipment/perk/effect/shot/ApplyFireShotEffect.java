package org.phantazm.zombies.equipment.perk.effect.shot;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.annotation.document.Description;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Tickable;
import org.phantazm.zombies.map.action.Action;

import java.util.*;

@Description("""
        An entity action that sets an entity on fire for a configurable amount of time, health, and damage interval.
        """)
@Model("zombies.perk.effect.shot_entity.apply_fire")
@Cache(false)
public class ApplyFireShotEffect implements Action<Entity>, Tickable {
    private final Data data;
    private final Tag<Long> timeTag;
    private final Tag<Long> timeSinceLastDamage;
    private final Deque<Entity> activeEntities;

    @FactoryMethod
    public ApplyFireShotEffect(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
        this.timeTag = Tag.Long(UUID.randomUUID().toString()).defaultValue(-1L);
        this.timeSinceLastDamage = Tag.Long(UUID.randomUUID().toString()).defaultValue(-1L);
        this.activeEntities = new LinkedList<>();
    }

    @Override
    public void perform(@NotNull Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            //can't set non-LivingEntity on fire as they have no health
            return;
        }

        entity.setTag(timeTag, System.currentTimeMillis());
        activeEntities.add(entity);
    }

    @Override
    public void tick(long time) {
        activeEntities.removeIf(entity -> process(entity, time));
    }

    private boolean process(Entity entity, long time) {
        if (entity.isRemoved()) {
            return true;
        }

        long lastApply = entity.getTag(timeTag);
        if (lastApply == -1) {
            //entity never should have been added to the queue
            stopFire(entity);
            return true;
        }

        if (time - lastApply / MinecraftServer.TICK_MS >= data.fireTicks) {
            //fire has expired
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

    private void doDamage(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.damage(DamageType.ON_FIRE, data.damage);
        }
    }

    private void stopFire(Entity entity) {
        entity.setOnFire(false);
        entity.removeTag(timeTag);
        entity.removeTag(timeSinceLastDamage);
    }

    @DataObject
    public record Data(int fireTicks, int damageInterval, float damage) {
    }
}
