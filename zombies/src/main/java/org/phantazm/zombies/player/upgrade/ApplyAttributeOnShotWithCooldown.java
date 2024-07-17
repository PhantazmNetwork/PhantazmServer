package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.EventListener;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.Cooldown;
import org.phantazm.core.tick.Activable;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.equipment.gun.shoot.GunHit;
import org.phantazm.zombies.event.equipment.EntitiesHitByGunEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Deque;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

@Model("zombies.upgrade.apply_attribute_on_shot_with_cooldown")
@Cache
public class ApplyAttributeOnShotWithCooldown implements PlayerUpgradeComponent {
    private final Data data;

    @FactoryMethod
    public ApplyAttributeOnShotWithCooldown(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }


    @Override
    public @NotNull PlayerUpgrade apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(zombiesPlayer, data);
    }

    private static final class Internal extends GuardedPlayerUpgrade {
        private Internal(ZombiesPlayer zombiesPlayer, Data data) {
            super(Activable.threadsafeWrapper(new Activable() {
                private final EventListener<EntitiesHitByGunEvent> listener = EventListener
                    .builder(EntitiesHitByGunEvent.class).handler(this::onShotByGun).build();

                private final Cooldown applyCooldown = Cooldown.cooldown();
                private final Attribute attribute = Attributes.get(data.attribute);
                private final UUID uuid = UUID.randomUUID();
                private final AttributeModifier modifier = new AttributeModifier(uuid, uuid.toString(), data.value, data.operation);

                private final Deque<HitTarget> targets = new ConcurrentLinkedDeque<>();

                private record HitTarget(Reference<LivingEntity> entityReference,
                    Cooldown cooldown) {
                }

                @Override
                public void start() {
                    zombiesPlayer.getScene().sceneNode().addListener(listener);
                }

                @Override
                public void tick(long time) {
                    applyCooldown.step();
                    targets.removeIf(target -> {
                        LivingEntity entity = target.entityReference.get();
                        if (entity == null || entity.isRemoved() || entity.isDead()) {
                            return true;
                        }

                        if (target.cooldown.step()) {
                            entity.getAttribute(attribute).removeModifier(uuid);
                            return true;
                        }

                        return false;
                    });
                }

                @Override
                public void end() {
                    zombiesPlayer.getScene().sceneNode().removeListener(listener);
                }

                private void onShotByGun(EntitiesHitByGunEvent event) {
                    if (!event.getShooter().getUuid().equals(zombiesPlayer.getUUID()) ||
                        !applyCooldown.takeCooldown(data.cooldown)) {
                        return;
                    }

                    for (GunHit hit : event.targets()) {
                        hit.entity().getAttribute(attribute).addModifier(modifier);

                        if (data.attributeDuration >= 0) {
                            targets.push(new HitTarget(new WeakReference<>(hit.entity()), Cooldown
                                .cooldown(data.attributeDuration)));
                        }
                    }
                }
            }), zombiesPlayer);
        }

        @Override
        public boolean needsTicking() {
            return true;
        }
    }

    @DataObject
    public record Data(int cooldown,
        int attributeDuration,
        @NotNull String attribute,
        double value,
        @NotNull AttributeOperation operation) {
    }
}
