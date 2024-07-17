package org.phantazm.zombies.equipment.perk.effect.shot;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.Tickable;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.AttributeUtils;
import org.phantazm.core.DamageUtils;
import org.phantazm.core.TagUtils;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.ExtraNodeKeys;
import org.phantazm.zombies.event.player.ZombiesPlayerProcFireEvent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

@Model("zombies.perk.effect.shot_entity.apply_fire")
@Cache(false)
public class ApplyFireShotEffect implements ShotEffect, Tickable {
    private final Data data;
    private final ZombiesScene scene;
    private final Tag<Long> lastDamageTicksTag;
    private final Deque<DamageTarget> activeEntities;

    @FactoryMethod
    public ApplyFireShotEffect(@NotNull Data data, @NotNull ZombiesScene scene) {
        this.data = data;
        this.scene = scene;
        this.lastDamageTicksTag = Tag.Long(TagUtils.uniqueTagName()).defaultValue(-1L);

        this.activeEntities = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void perform(@NotNull Entity entity, @NotNull ZombiesPlayer zombiesPlayer) {
        perform(entity, zombiesPlayer, 1.0D);
    }

    public void perform(@NotNull Entity entity, @NotNull ZombiesPlayer zombiesPlayer, double effectScale) {
        Optional<Player> playerOptional = zombiesPlayer.getPlayer();
        if (playerOptional.isEmpty()) {
            return;
        }

        if (!(entity instanceof LivingEntity livingEntity) || (entity instanceof Mob mob &&
            mob.data().extra().getBooleanOrDefault(ExtraNodeKeys.RESIST_FIRE, false))) {
            //can't set non-LivingEntity on fire as they have no health
            return;
        }

        Player player = playerOptional.get();
        livingEntity.setFireForDuration((int) Math.round(AttributeUtils.computeWithBase(data.fireTicks, player
            .getAttribute(Attributes.FIRE_APPLY_DURATION)) * effectScale));

        TagHandler tags = TagUtils.sceneLocalTags(entity, scene);
        boolean alreadyActive = tags.getTag(lastDamageTicksTag) != -1;
        tags.setTag(lastDamageTicksTag, 0L);

        if (!alreadyActive) {
            int interval = Math.round(AttributeUtils.computeWithBase(data.damageInterval, player
                .getAttribute(Attributes.FIRE_DAMAGE_APPLY_INTERVAL)));

            activeEntities.add(new DamageTarget(new WeakReference<>(player), new WeakReference<>(livingEntity),
                zombiesPlayer, interval, effectScale));
        }
    }

    @Override
    public void tick(long time) {
        activeEntities.removeIf(this::shouldRemoveTarget);
    }

    private boolean shouldRemoveTarget(DamageTarget target) {
        LivingEntity entity = target.target.get();
        if (entity == null) {
            return true;
        }

        if (entity.isRemoved() || entity.isDead() || !entity.isOnFire()) {
            TagUtils.removeSceneLocalTag(entity, scene, lastDamageTicksTag);
            return true;
        }

        TagHandler tags = TagUtils.sceneLocalTags(entity, scene);
        long lastDamageTicks = tags.updateAndGetTag(this.lastDamageTicksTag, oldValue -> oldValue + 1);

        if (lastDamageTicks >= target.interval) {
            doDamage(entity, target.damager.get(), target.player, target.scale);
            tags.setTag(this.lastDamageTicksTag, 0L);
        }

        return false;
    }

    private void doDamage(LivingEntity target, Player damager, ZombiesPlayer player, double scale) {
        if (damager == null) {
            return;
        }

        float damage = AttributeUtils.computeWithBase(data.damage, damager.getAttribute(Attributes.FIRE_APPLY_DAMAGE));

        scene.broadcastCancellable(new ZombiesPlayerProcFireEvent(damager, player, damage, target), event -> {
            target.getAcquirable().sync(self -> DamageUtils.damage(data.damageType, (LivingEntity) self, amount ->
                    new Damage(DamageType.ON_FIRE, null, damager, null, amount),
                (float) (event.damageAmount() * scale), data.bypassArmor));
        });
    }

    private record DamageTarget(Reference<Player> damager,
        Reference<LivingEntity> target,
        ZombiesPlayer player,
        int interval,
        double scale) {
    }

    @Default("""
        {
          damageType='damage.fire'
        }
        """)
    @DataObject
    public record Data(
        int fireTicks,
        int damageInterval,
        float damage,
        boolean bypassArmor,
        String damageType) {
    }
}
