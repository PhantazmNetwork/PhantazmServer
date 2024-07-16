package org.phantazm.zombies.equipment.gun.shoot.handler;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.DamageUtils;
import org.phantazm.core.TagUtils;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.ExtraNodeKeys;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.shoot.GunHit;
import org.phantazm.zombies.equipment.gun.shoot.GunShot;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Deque;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * A {@link ShotHandler} that sets {@link Entity}s on fire.
 */
@Model("zombies.gun.shot_handler.ignite")
@Cache(false)
public class IgniteShotHandler implements ShotHandler {
    private record Target(Reference<LivingEntity> entity,
        Reference<Entity> attacker) {
    }

    private final Data data;
    private final ZombiesScene scene;

    private final Tag<Long> lastFireDamageTicksTag;
    private final Deque<Target> targets;

    /**
     * Creates an {@link IgniteShotHandler}.
     *
     * @param data The {@link IgniteShotHandler}'s {@link Data}
     */
    @FactoryMethod
    public IgniteShotHandler(@NotNull Data data, @NotNull ZombiesScene scene) {
        this.data = data;
        this.scene = scene;

        this.lastFireDamageTicksTag = Tag.Long(TagUtils.uniqueTagName()).defaultValue(-1L);
        this.targets = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void handle(@NotNull Gun gun, @NotNull GunState state, @NotNull Entity attacker,
        @NotNull Collection<UUID> previousHits, @NotNull GunShot shot) {
        setFire(shot.gunHits(), attacker);
    }

    private void setFire(Collection<GunHit> hits, @NotNull Entity attacker) {
        for (GunHit target : hits) {
            LivingEntity entity = target.entity();
            if (!(entity instanceof Mob mob)) {
                return;
            }

            if (mob.data().extra().getBooleanOrDefault(ExtraNodeKeys.RESIST_FIRE, false)) {
                continue;
            }

            entity.setFireForDuration(target.isHeadshot() ? data.headshotFireTicks : data.normalFireTicks);

            TagHandler tags = TagUtils.sceneLocalTags(entity, scene);
            long lastFireDamageTicks = tags.getTag(lastFireDamageTicksTag);
            boolean alreadyActive = lastFireDamageTicks != -1;
            tags.setTag(lastFireDamageTicksTag, ++lastFireDamageTicks);

            if (!alreadyActive) {
                targets.add(new Target(new WeakReference<>(entity), new WeakReference<>(attacker)));
            }
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {
        targets.removeIf(target -> {
            LivingEntity entity = target.entity.get();
            if (entity == null) {
                return true;
            }

            if (entity.isDead() || entity.isRemoved() || !entity.isOnFire()) {
                remove(entity);
                return true;
            }

            TagHandler tags = TagUtils.sceneLocalTags(entity, scene);
            if (tags.getTag(this.lastFireDamageTicksTag) >= data.damageInterval) {
                damage(entity, target.attacker.get());
                tags.setTag(this.lastFireDamageTicksTag, 0L);
            }

            return false;
        });
    }

    private void remove(LivingEntity target) {
        TagUtils.removeSceneLocalTag(target, scene, lastFireDamageTicksTag);
    }

    private void damage(LivingEntity target, Entity attacker) {
        DamageUtils.damage(data.damageType, target, amount ->
            new Damage(DamageType.ON_FIRE, null, attacker, null, amount), data.damage, data.bypassArmor);
    }

    @Default("""
        {
          bypassArmor=false,
          damageType=null
        }
        """)
    @DataObject
    public record Data(
        int normalFireTicks,
        int headshotFireTicks,
        int damageInterval,
        float damage,
        boolean bypassArmor,
        String damageType) {

    }

}
