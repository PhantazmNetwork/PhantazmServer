package org.phantazm.zombies.player.upgrade.trigger;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.event.RoundStartEvent;
import org.phantazm.zombies.event.entity.EntityAttributeModifierRemoveEvent;
import org.phantazm.zombies.event.entity.MobBreakWindowEvent;
import org.phantazm.zombies.event.entity.MobDeathEvent;
import org.phantazm.zombies.event.entity.MobSetupEvent;
import org.phantazm.zombies.event.equipment.*;
import org.phantazm.zombies.event.player.*;
import org.phantazm.zombies.event.trait.*;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.effect.UpgradeEffect;
import org.phantazm.zombies.player.upgrade.trigger.filter.TriggerFilter;
import org.phantazm.zombies.player.upgrade.trigger.filter.TriggerFilterComponent;

@Model("zombies.upgrade.trigger.event")
@Cache
public class EventTrigger implements UpgradeTriggerComponent {
    private final Class<? extends Event> eventClass;
    private final TriggerFilterComponent triggerFilterComponent;

    @FactoryMethod
    public EventTrigger(@NotNull Data data, @NotNull @Child("eventFilter") TriggerFilterComponent triggerFilterComponent) {
        this.eventClass = data.event.eventClass();
        this.triggerFilterComponent = triggerFilterComponent;
    }

    @Override
    public @NotNull UpgradeTrigger apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(zombiesPlayer, eventClass, triggerFilterComponent.apply(injectionStore, zombiesPlayer));
    }

    private static final class Internal implements UpgradeTrigger {
        private final ZombiesPlayer zombiesPlayer;
        private final EventListener<? extends Event> listener;
        private final TriggerFilter triggerFilter;

        private volatile ArmData target;

        private record ArmData(PlayerUpgrade upgrade,
            UpgradeEffect effect) {
        }

        private Internal(ZombiesPlayer zombiesPlayer, Class<? extends Event> cls, TriggerFilter triggerFilter) {
            this.zombiesPlayer = zombiesPlayer;
            this.listener = EventListener.builder(cls).handler(this::handle).filter(this::filter).build();
            this.triggerFilter = triggerFilter;
        }

        @Override
        public void arm(@NotNull PlayerUpgrade upgrade, @NotNull UpgradeEffect effect) {
            this.target = new ArmData(upgrade, effect);
            zombiesPlayer.getScene().sceneNode().addListener(listener);
        }

        @Override
        public void disarm() {
            zombiesPlayer.getScene().sceneNode().removeListener(listener);
        }

        @Override
        public boolean needsTicking() {
            return false;
        }

        private boolean filter(Event event) {
            ArmData armData = this.target;
            if (armData == null) {
                return false;
            }

            return triggerFilter.test(armData.upgrade, zombiesPlayer, TriggerData.of(event));
        }

        private void handle(Event event) {
            ArmData armData = this.target;
            if (armData != null) {
                armData.effect.apply(armData.upgrade, zombiesPlayer, TriggerData.of(event));
            }
        }
    }

    public enum EventType {
        ENTITY_DAMAGE(EntityDamageEvent.class),
        ENTITY_DAMAGE_BY_GUN(EntityDamageByGunEvent.class),
        ENTITIES_HIT_BY_GUN(EntitiesHitByGunEvent.class),
        ATTRIBUTE(AttributeEvent.class),
        ENTITY_ATTRIBUTE_REMOVE(EntityAttributeModifierRemoveEvent.class),
        MOB_BREAK_WINDOW(MobBreakWindowEvent.class),
        MOB_DEATH(MobDeathEvent.class),
        MOB_SETUP(MobSetupEvent.class),
        GUN_LOSE_AMMO(GunLoseAmmoEvent.class),
        GUN_REFILL(GunRefillEvent.class),
        GUN_TARGET_SELECT(GunTargetSelectEvent.class),
        ZOMBIES_PLAYER(ZombiesPlayerEvent.class),
        ZOMBIES_PLAYER_DAMAGE(ZombiesPlayerDamageEvent.class),
        ZOMBIES_PLAYER_KNOCK(ZombiesPlayerKnockEvent.class),
        ZOMBIES_PLAYER_POST_KNOCK(ZombiesPlayerPostKnockEvent.class),
        ZOMBIES_PLAYER_DISMOUNT(ZombiesPlayerDismountEvent.class),
        ZOMBIES_PLAYER_SNEAK(ZombiesPlayerSneakEvent.class),
        ZOMBIES_PLAYER_KILL_MOB(ZombiesPlayerKillMobEvent.class),
        ZOMBIES_PLAYER_MELEE_ENTITY(ZombiesPlayerMeleeEntityEvent.class),
        ZOMBIES_PLAYER_MODIFY_ATTRIBUTE(ZombiesPlayerModifyAttributeEvent.class),
        ZOMBIES_PLAYER_OPEN_DOOR(ZombiesPlayerOpenDoorEvent.class),
        ZOMBIES_PLAYER_PROC_FIRE(ZombiesPlayerProcFireEvent.class),
        ZOMBIES_PLAYER_REPAIR_WINDOW(ZombiesPlayerRepairWindowEvent.class),
        ZOMBIES_PLAYER_REVIVE(ZombiesPlayerReviveEvent.class),
        ZOMBIES_PLAYER_START_REVIVE(ZombiesPlayerStartReviveEvent.class),
        ZOMBIES_PLAYER_END_REVIVE(ZombiesPlayerEndReviveEvent.class),
        ZOMBIES_PLAYER_MODIFY_UPGRADE(ZombiesPlayerModifyUpgrade.class),
        ZOMBIES_PLAYER_USE_EQUIPMENT(ZombiesPlayerUseEquipmentEvent.class),
        ROUND_START(RoundStartEvent.class),
        DAMAGE(DamageEvent.class),
        ENTITY_TARGET(EntityTargetEvent.class),
        LIVING_TARGET(LivingTargetEvent.class),
        MOB_TARGET(MobTargetEvent.class),
        SETTABLE_ATTRIBUTE(SettableAttributeEvent.class),
        SETTABLE_DAMAGE_AMOUNT(SettableDamageAmountEvent.class),
        SHOOTER(ShooterEvent.class),
        WINDOW(WindowEvent.class),
        ;

        private final Class<? extends Event> eventClass;

        EventType(Class<? extends Event> eventClass) {
            this.eventClass = eventClass;
        }

        public @NotNull Class<? extends Event> eventClass() {
            return eventClass;
        }
    }

    @DataObject
    public record Data(@NotNull EventType event) {
    }
}
