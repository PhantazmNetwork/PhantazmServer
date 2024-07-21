package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.tick.Activable;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.ExtraNodeKeys;
import org.phantazm.zombies.event.equipment.EntityDamageByGunEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.*;

@Model("zombies.upgrade.apply_limited_attribute_on_shot")
@Cache
public class ApplyLimitedAttributeOnShot implements PlayerUpgradeComponent {
    private final Data data;

    @FactoryMethod
    public ApplyLimitedAttributeOnShot(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull PlayerUpgrade apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(zombiesPlayer, data);
    }

    private static final class Internal extends GuardedPlayerUpgrade {
        private Internal(ZombiesPlayer zombiesPlayer, Data data) {
            super(Activable.threadsafeWrapper(new Activable() {
                private final EventListener<EntityDamageByGunEvent> listener = EventListener
                    .builder(EntityDamageByGunEvent.class).handler(this::onShotByGun).build();

                private final UUID uuid = UUID.randomUUID();

                private int attributeLevel;
                private int tickTimer;
                private final Deque<Integer> scheduler = new ArrayDeque<>();

                @Override
                public void start() {
                    zombiesPlayer.getScene().sceneNode().addListener(listener);
                }

                @Override
                public void end() {
                    zombiesPlayer.getScene().sceneNode().removeListener(listener);
                }

                private void onShotByGun(EntityDamageByGunEvent event) {
                    if (!event.getShooter().getUuid().equals(zombiesPlayer.getUUID())) {
                        // shooter is not the perk owner
                        return;
                    }

                    Entity entity = event.getEntity();
                    if (entity instanceof Mob && resistsSlowDown((Mob) entity)) {
                        // entity resisted the effect
                        return;
                    } else if (attributeLevel <= data.maxAttributeLevel * 2){
                        attributeLevel++;
                        scheduler.add(tickTimer + data.attributeDuration);
                    }
                }

                @Override
                public void tick(long time) {
                    tickTimer++;

                    if (attributeLevel > 0) {
                        while (!scheduler.isEmpty() && tickTimer >= scheduler.peek()) {
                            scheduler.pop();
                            attributeLevel--;
                        }

                        applyAttributeLevel();
                    }
                }

                private static boolean resistsSlowDown(Mob mob) {
                    return mob.data().extra().getBooleanOrDefault(
                        ExtraNodeKeys.RESIST_SLOW_DOWN, false);
                }

                private void applyAttributeLevel() {
                    int actualAttributeLevel = Math.min(attributeLevel, data.maxAttributeLevel);

                    Optional<Player> playerOptional = zombiesPlayer.getPlayer();
                    if (playerOptional.isEmpty()) {
                        return;
                    }

                    AttributeInstance playerAttribute = playerOptional.get().getAttribute(Attributes.get(data.attribute));

                    playerAttribute.removeModifier(uuid);
                    AttributeModifier modifier = new AttributeModifier(uuid, uuid.toString(), data.value * actualAttributeLevel, data.operation);

                    playerAttribute.addModifier(modifier);
                }

            }), zombiesPlayer);
        }

        @Override
        public boolean needsTicking() {
            return true;
        }
    }

    @DataObject
    public record Data(
        @NotNull String attribute,
        int maxAttributeLevel,
        int attributeDuration,
        double value,
        @NotNull AttributeOperation operation) {
    }
}
