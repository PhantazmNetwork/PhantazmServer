package org.phantazm.zombies.mob.skill.hit_action;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.CancellableState;
import org.phantazm.commons.TickTaskScheduler;
import org.phantazm.commons.TickableTask;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Model("zombies.mob.skill.projectile.hit_action.attribute_modifier")
@Cache(false)
public class AttributeModifierAction implements ProjectileHitEntityAction {
    private final Data data;
    private final TickTaskScheduler taskScheduler;
    private final Map<? super UUID, ? extends ZombiesPlayer> playerMap;

    @FactoryMethod
    public AttributeModifierAction(@NotNull Data data, @NotNull MapObjects mapObjects) {
        this.data = data;
        this.taskScheduler = mapObjects.taskScheduler();
        this.playerMap = mapObjects.module().playerMap();
    }

    @Override
    public void perform(@Nullable PhantazmMob shooter, @NotNull Entity projectile, @NotNull Entity target) {
        if (!(target instanceof LivingEntity livingEntity)) {
            return;
        }

        ZombiesPlayer zombiesPlayer = playerMap.get(target.getUuid());
        UUID modifier = UUID.randomUUID();

        if (zombiesPlayer != null) {
            zombiesPlayer.registerCancellable(CancellableState.named(modifier, () -> {
                zombiesPlayer.getPlayer().ifPresent(player -> {
                    player.getAttribute(Objects.requireNonNullElse(Attribute.fromKey(data.attribute), Attributes.NIL))
                        .addModifier(new AttributeModifier(modifier, modifier.toString(), data.amount,
                            data.attributeOperation));
                });
            }, () -> {
                zombiesPlayer.getPlayer().ifPresent(player -> {
                    player.getAttribute(Objects.requireNonNullElse(Attribute.fromKey(data.attribute), Attributes.NIL))
                        .removeModifier(modifier);
                });
            }), true);

            taskScheduler.scheduleTaskNow(TickableTask.afterTicks(data.duration, () -> {
                zombiesPlayer.removeCancellable(modifier);
            }, () -> {
                zombiesPlayer.removeCancellable(modifier);
            }));
            return;
        }

        livingEntity.getAttribute(Objects.requireNonNullElse(Attribute.fromKey(data.attribute), Attributes.NIL))
            .addModifier(
                new AttributeModifier(modifier, modifier.toString(), data.amount, data.attributeOperation));
        livingEntity.scheduler().scheduleTask(() -> {
            livingEntity.getAttribute(Objects.requireNonNullElse(Attribute.fromKey(data.attribute), Attributes.NIL))
                .removeModifier(modifier);
        }, TaskSchedule.tick((int) data.duration), TaskSchedule.stop());
    }

    @DataObject
    public record Data(
        @NotNull String attribute,
        float amount,
        @NotNull AttributeOperation attributeOperation,
        long duration) {
    }
}
