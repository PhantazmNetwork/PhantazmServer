package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.event.EventListener;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.TagUtils;
import org.phantazm.core.tick.Activable;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.event.player.ZombiesPlayerIgniteTargetEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

@Model("zombies.upgrade.fire_aoe_spread")
@Cache
public class FireAOESpreadUpgrade implements PlayerUpgradeComponent {
    private final Data data;

    @FactoryMethod
    public FireAOESpreadUpgrade(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull PlayerUpgrade apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer player) {
        return new Internal(player, data);
    }

    private static class Internal extends GuardedPlayerUpgrade {
        private Internal(ZombiesPlayer zombiesPlayer, Data data) {
            super(Activable.threadsafeWrapper(new Activable() {
                private final EventListener<ZombiesPlayerIgniteTargetEvent> event = EventListener.builder(ZombiesPlayerIgniteTargetEvent.class)
                    .handler(this::handleMobIgnite).build();
                private static final Tag<Boolean> AOE_IGNITED = Tag.Boolean(TagUtils.uniqueTagName()).defaultValue(false);

                @Override
                public void start() {
                    zombiesPlayer.getScene().sceneNode().addListener(event);
                }

                @Override
                public void end() {
                    zombiesPlayer.getScene().sceneNode().removeListener(event);
                }

                private void handleMobIgnite(ZombiesPlayerIgniteTargetEvent event) {
                    if (event.getZombiesPlayer() != zombiesPlayer || event.target().getTag(AOE_IGNITED)) {
                        return;
                    }

                    zombiesPlayer.getScene().instance().getEntityTracker().nearbyEntities(event.target().getPosition(),
                        data.radius, EntityTracker.Target.LIVING_ENTITIES, entity -> {
                            if (entity == event.target() || !(entity instanceof Mob mob) || mob.getTag(AOE_IGNITED)) {
                                return;
                            }

                            mob.setTag(AOE_IGNITED, true);
                            event.cause().perform(mob, zombiesPlayer);
                            mob.removeTag(AOE_IGNITED);
                        });
                }
            }), zombiesPlayer);
        }

        @Override
        public boolean needsTicking() {
            return false;
        }
    }

    @DataObject
    public record Data(double radius) {

    }
}
