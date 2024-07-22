package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.EventListener;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.TagUtils;
import org.phantazm.core.tick.Activable;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.equipment.perk.effect.shot.ShotEffect;
import org.phantazm.zombies.event.entity.EntityAttributeModifierRemoveEvent;
import org.phantazm.zombies.event.player.ZombiesPlayerKillMobEvent;
import org.phantazm.zombies.event.player.ZombiesPlayerModifyAttributeEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

@Model("zombies.upgrade.spread_attribute_on_death")
@Cache
public class SpreadAttributeOnDeathUpgrade implements PlayerUpgradeComponent {
    private final Data data;

    @FactoryMethod
    public SpreadAttributeOnDeathUpgrade(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull PlayerUpgrade apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer player) {
        return new Internal(player, data);
    }

    private static class Internal extends GuardedPlayerUpgrade {
        private Internal(ZombiesPlayer zombiesPlayer, Data data) {
            super(Activable.threadsafeWrapper(new Activable() {
                private static final Tag<Boolean> HAS_SPREAD_EFFECT = Tag.Boolean(TagUtils.uniqueTagName()).defaultValue(false);
                private static final Tag<UUID> EFFECT_TAG = Tag.UUID(TagUtils.uniqueTagName());

                private static final ExtensionHolder.Key<ShotEffect> EFFECT_KEY = ExtensionHolder.requestKey(ShotEffect.class);

                private static final UUID RANDOM_UUID = UUID.randomUUID();
                private static final String NAME = RANDOM_UUID.toString();

                private final EventListener<ZombiesPlayerKillMobEvent> killMobEvent = EventListener
                    .builder(ZombiesPlayerKillMobEvent.class).handler(this::handleMobDeath).build();

                private final EventListener<ZombiesPlayerModifyAttributeEvent> modifyAttributeEvent = EventListener
                    .builder(ZombiesPlayerModifyAttributeEvent.class).handler(this::handleModifyAttribute).build();

                private final EventListener<EntityAttributeModifierRemoveEvent> wearOffEvent = EventListener
                    .builder(EntityAttributeModifierRemoveEvent.class).handler(this::handleWearOffEvent).build();

                @Override
                public void start() {
                    zombiesPlayer.getScene().sceneNode().addListener(killMobEvent);
                    zombiesPlayer.getScene().sceneNode().addListener(modifyAttributeEvent);
                    zombiesPlayer.getScene().sceneNode().addListener(wearOffEvent);
                }

                @Override
                public void end() {
                    zombiesPlayer.getScene().sceneNode().removeListener(killMobEvent);
                    zombiesPlayer.getScene().sceneNode().removeListener(modifyAttributeEvent);
                    zombiesPlayer.getScene().sceneNode().removeListener(wearOffEvent);
                }

                private void handleMobDeath(ZombiesPlayerKillMobEvent event) {
                    if (event.zombiesPlayer() != zombiesPlayer) {
                        return;
                    }

                    Instance instance = event.target().getInstance();
                    if (instance == null) {
                        return;
                    }

                    ShotEffect effect = event.target().extensions().get(EFFECT_KEY);
                    if (effect == null || data.amount == 0) {
                        return;
                    }

                    Pos center = event.target().getPosition();
                    instance.getEntityTracker().nearbyEntitiesUntil(center, data.radius,
                        EntityTracker.Target.LIVING_ENTITIES, new Predicate<>() {
                            private int amount;

                            @Override
                            public boolean test(LivingEntity otherMob) {
                                if (!(otherMob instanceof Mob mob) || otherMob == event.target() ||
                                    mob.tagHandler().getAndUpdateTag(HAS_SPREAD_EFFECT, ignored -> true)) {
                                    return false;
                                }

                                effect.perform(mob, zombiesPlayer);

                                mob.getAttribute(Attributes.get(data.attribute))
                                    .addModifier(new AttributeModifier(RANDOM_UUID, NAME, data.amount, data.operation));

                                if (data.limit < 0) {
                                    return false;
                                }

                                return ++amount >= data.limit;
                            }
                        });
                }

                private void handleModifyAttribute(ZombiesPlayerModifyAttributeEvent event) {
                    if (event.zombiesPlayer() != zombiesPlayer || !(event.target() instanceof Mob mob) ||
                        !data.spreadAttributes.contains(event.attribute().key())) {
                        return;
                    }

                    if (mob.getTag(HAS_SPREAD_EFFECT)) {
                        event.setAttributeAmount((float) (event.attributeAmount() * data.effectScale));
                        mob.setTag(EFFECT_TAG, event.attributeUuid());
                        return;
                    }

                    mob.extensions().set(EFFECT_KEY, event.cause());
                }

                private void handleWearOffEvent(EntityAttributeModifierRemoveEvent event) {
                    if (Objects.equals(event.attributeUuid(), event.getEntity().getTag(EFFECT_TAG))) {
                        Mob mob = (Mob) event.getEntity();
                        mob.extensions().remove(EFFECT_KEY);
                        mob.removeTag(HAS_SPREAD_EFFECT);
                        mob.removeTag(EFFECT_TAG);

                        if (data.removeModifierOnWearOff) {
                            mob.getAttribute(Attributes.get(data.attribute)).removeModifier(RANDOM_UUID);
                        }
                    }
                }
            }), zombiesPlayer);
        }

        @Override
        public boolean needsTicking() {
            return false;
        }
    }

    @DataObject
    public record Data(double radius,
        double effectScale,
        @NotNull Set<String> spreadAttributes,
        @NotNull String attribute,
        float amount,
        @NotNull AttributeOperation operation,
        int limit,
        boolean removeModifierOnWearOff) {

    }
}
