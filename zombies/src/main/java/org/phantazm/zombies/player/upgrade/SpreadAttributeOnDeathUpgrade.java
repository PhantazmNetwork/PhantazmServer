package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.coordinate.Pos;
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
import org.phantazm.zombies.equipment.perk.effect.shot.ShotEffect;
import org.phantazm.zombies.event.mob.MobAttributeWearOffEvent;
import org.phantazm.zombies.event.player.ZombiesPlayerKillMobEvent;
import org.phantazm.zombies.event.player.ZombiesPlayerModifyAttributeEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Set;
import java.util.UUID;

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
                private static final Tag<Boolean> SCALE_EFFECT = Tag.Boolean(TagUtils.uniqueTagName()).defaultValue(false);
                private static final ExtensionHolder.Key<ShotEffect> EFFECT_KEY = ExtensionHolder.requestKey(ShotEffect.class);
                private static final UUID RANDOM_UUID = UUID.randomUUID();
                private static final String NAME = RANDOM_UUID.toString();

                private final EventListener<ZombiesPlayerKillMobEvent> killMobEvent = EventListener
                    .builder(ZombiesPlayerKillMobEvent.class).handler(this::handleMobDeath).build();
                private final EventListener<ZombiesPlayerModifyAttributeEvent> modifyAttributeEvent = EventListener
                    .builder(ZombiesPlayerModifyAttributeEvent.class).handler(this::handleModifyAttribute).build();
                private final EventListener<MobAttributeWearOffEvent> wearOffEvent = EventListener
                    .builder(MobAttributeWearOffEvent.class).handler(this::handleWearOffEvent).build();

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
                    if (event.getZombiesPlayer() != zombiesPlayer) {
                        return;
                    }

                    Pos center = event.target().getPosition();
                    Instance instance = event.target().getInstance();
                    if (instance == null) {
                        return;
                    }

                    ShotEffect effect = event.target().extensions().get(EFFECT_KEY);
                    if (effect == null) {
                        return;
                    }

                    instance.getEntityTracker().nearbyEntities(center, data.radius,
                        EntityTracker.Target.LIVING_ENTITIES, otherMob -> {
                            if (!(otherMob instanceof Mob mob) || otherMob == event.target() ||
                                mob.tagHandler().getAndUpdateTag(SCALE_EFFECT, ignored -> true)) {
                                return;
                            }

                            effect.perform(mob, zombiesPlayer);

                            Attribute attribute = Attribute.fromKey(data.attribute);
                            if (attribute != null) {
                                mob.getAttribute(attribute).addModifier(new AttributeModifier(RANDOM_UUID, NAME, data.amount,
                                    data.operation));
                            }
                        });
                }

                private void handleModifyAttribute(ZombiesPlayerModifyAttributeEvent event) {
                    if (event.getZombiesPlayer() != zombiesPlayer || !(event.target() instanceof Mob mob) ||
                        !data.spreadAttributes.contains(event.attribute().key())) {
                        return;
                    }

                    if (mob.getTag(SCALE_EFFECT)) {
                        event.setAttributeAmount((float) (event.attributeAmount() * data.effectScale));
                        return;
                    }

                    mob.extensions().set(EFFECT_KEY, event.cause());
                }

                private void handleWearOffEvent(MobAttributeWearOffEvent event) {
                    if ((event.target() instanceof Mob mob) && event.removedModifier().getId().equals(RANDOM_UUID)) {
                        mob.tagHandler().removeTag(SCALE_EFFECT);
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
        @NotNull AttributeOperation operation) {

    }
}
