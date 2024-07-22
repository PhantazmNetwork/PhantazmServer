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

/**
 * Developed for the Frozen Bullets and Quick Fire synergy. When a player
 * shoots a zombie, this class applies a modifier that can stack up to a
 * limited level and where each level expires after some time.
 */
@Model("zombies.upgrade.apply_limited_attribute_on_shot")
@Cache
public class ApplyLimitedAttributeOnShot implements PlayerUpgradeComponent {

    /** A record of configurable data fields for this upgrade. */
    private final Data data;

    /** I think this might make sure that the data fields aren't null */
    @FactoryMethod
    public ApplyLimitedAttributeOnShot(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }
    @Override
    public @NotNull PlayerUpgrade apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(zombiesPlayer, data);
    }

    private static final class Internal extends GuardedPlayerUpgrade {
        @Override
        public boolean needsTicking() {
            return true;
        }

        /**
         * Private constructor.
         * @param zombiesPlayer the player who owns this upgrade
         * @param data a record of configurable data fields for this upgrade
         */
        private Internal(ZombiesPlayer zombiesPlayer, Data data) {
            super(Activable.threadsafeWrapper(new Activable() {
                /** Builds event listener and sets handler. */
                private final EventListener<EntityDamageByGunEvent> listener = EventListener
                    .builder(EntityDamageByGunEvent.class).handler(this::onEntityShot).build();

                /** This upgrade's UUID for representing it as an AttributeModifier. */
                private final UUID uuid = UUID.randomUUID();

                /** Queue for storing the scheduled times of subtracting numberOfStacks. */
                private final Deque<Integer> schedule = new ArrayDeque<>();

                /** How many stacks of this upgrade's attribute modifier the player has. */
                private int numberOfStacks;

                /** How many ticks passed since the player got this upgrade. */
                private int tickTimer;

                @Override
                public void start() {
                    zombiesPlayer.getScene().sceneNode().addListener(listener);
                }

                @Override
                public void end() {
                    zombiesPlayer.getScene().sceneNode().removeListener(listener);
                }

                @Override
                public void tick(long time) {
                    tickTimer++;

                    if (numberOfStacks > 0) {
                        while (!schedule.isEmpty() && tickTimer >= schedule.peek()) {
                            schedule.pop();
                            numberOfStacks--;
                        }
                        applyModifierLevel();
                    }
                }

                /**
                 * Called every time an entity receives damage from a gun.
                 * Increments numberOfStacks and schedules it to be decremented
                 * after a certain number of ticks.
                 */
                private void onEntityShot(EntityDamageByGunEvent event) {

                    Entity shooter = event.getShooter();
                    Entity target = event.getEntity();

                    if (shooter.getUuid().equals(zombiesPlayer.getUUID()) &&
                        numberOfStacks < data.maxStoredStacks &&
                        !targetIsResistant(target))
                    {
                        schedule.add(tickTimer + data.attributeDuration);
                        numberOfStacks++;
                    }
                }

                /**
                 * Returns true if the target resists the debuff relevant to this upgrade.
                 * (e.g. the target has RESIST_SLOW_DOWN)
                 * FIXME: hardcoded to only consider frozen bullets resistance
                 * @param target the entity being damaged
                 */
                private boolean targetIsResistant(Entity target) {
                    if (target instanceof Mob mob) {
                        return mob.data().extra().getBooleanOrDefault(
                            ExtraNodeKeys.RESIST_SLOW_DOWN, false);
                    } else {
                        return false;
                    }
                }

                /**
                 * Updates the attribute relevant to this upgrade by applying
                 * the correct modifier level.
                 */
                private void applyModifierLevel() {
                    // remove outdated modifier
                    Optional<Player> playerOptional = zombiesPlayer.getPlayer();
                    if (playerOptional.isEmpty()) {
                        return;
                    }
                    AttributeInstance playerAttribute = playerOptional.get().getAttribute(Attributes.get(data.attribute));
                    playerAttribute.removeModifier(uuid);

                    // add updated modifier
                    int modifierLevel = Math.min(numberOfStacks, data.maxEffectiveStacks);
                    AttributeModifier modifier = new AttributeModifier(uuid, uuid.toString(), data.value * modifierLevel, data.operation);
                    playerAttribute.addModifier(modifier);
                }

            }), zombiesPlayer);
        }
    }

    /**
     * Record constructor.
     * @param attribute Minecraft attribute that should be modified
     * @param maxEffectiveStacks Max number of stacks before the attribute modifier can't go any higher
     * @param maxStoredStacks Max number of stacks stored before we stop keeping track
     * @param attributeDuration How long one stack of the attribute modifier lasts before expiring
     * @param value How much to modify the attribute by
     * @param operation How to modify the attribute
     */
    @DataObject
    public record Data(
        @NotNull String attribute,
        int maxEffectiveStacks,
        int maxStoredStacks,
        int attributeDuration,
        double value,
        @NotNull AttributeOperation operation) {
    }
}
