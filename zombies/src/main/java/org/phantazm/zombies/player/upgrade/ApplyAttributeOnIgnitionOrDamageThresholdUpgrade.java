package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.EventListener;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.Cooldown;
import org.phantazm.core.DamageUtils;
import org.phantazm.core.tick.Activable;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.event.player.ZombiesPlayerDamageEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Model("zombies.upgrade.apply_attribute_on_ignition_or_damage_threshold")
@Cache
public class ApplyAttributeOnIgnitionOrDamageThresholdUpgrade implements PlayerUpgradeComponent {
    private final Data data;

    @FactoryMethod
    public ApplyAttributeOnIgnitionOrDamageThresholdUpgrade(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull PlayerUpgrade apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(zombiesPlayer, data);
    }

    private static class Internal extends GuardedPlayerUpgrade {

        private Internal(ZombiesPlayer zombiesPlayer, Data data) {
            super(Activable.threadsafeWrapper(new Activable() {
                private final EventListener<ZombiesPlayerDamageEvent> damageEventListener = EventListener
                    .builder(ZombiesPlayerDamageEvent.class).handler(this::onPlayerTakeDamage).build();

                private int counter;

                private final UUID uuid = UUID.randomUUID();
                private final AttributeModifier attributeModifier = new AttributeModifier(uuid, uuid.toString(),
                    data.amount, data.operation);
                private final Attribute attribute = Objects.requireNonNullElse(Attributes.get(data.attribute), Attributes.NIL);

                private final AtomicBoolean onFire = new AtomicBoolean();
                private final Cooldown effectCooldown = Cooldown.cooldown();
                private final AtomicReference<ActivationType> activationType = new AtomicReference<>(ActivationType.NOT_ACTIVATED);

                private enum ActivationType {
                    BY_FIRE,
                    BY_DAMAGE,
                    NOT_ACTIVATED
                }

                @Override
                public void start() {
                    zombiesPlayer.getScene().sceneNode().addListener(damageEventListener);
                }

                @Override
                public void tick(long time) {
                    if (data.fireTriggers && ((++counter) & 4) == 0) {
                        Optional<Player> player = zombiesPlayer.getPlayer();

                        if (player.isPresent() && player.get().isOnFire()) {
                            if (onFire.compareAndSet(false, true)) {
                                onIgnited();
                            }
                        } else if (onFire.compareAndSet(true, false)) {
                            onExtinguished();
                        }
                    }

                    if (effectCooldown.step() && activationType.compareAndSet(ActivationType.BY_DAMAGE, ActivationType.NOT_ACTIVATED)) {
                        zombiesPlayer.getPlayer().ifPresent(this::deactivate);
                    }
                }

                @Override
                public void end() {
                    zombiesPlayer.getScene().sceneNode().removeListener(damageEventListener);
                    zombiesPlayer.getPlayer().ifPresent(player -> player.getAttribute(attribute)
                        .removeModifier(attributeModifier));

                    activationType.set(ActivationType.NOT_ACTIVATED);
                    effectCooldown.reset();
                }

                private void onIgnited() {
                    if (activationType.compareAndSet(ActivationType.NOT_ACTIVATED, ActivationType.BY_FIRE)) {
                        zombiesPlayer.getPlayer().ifPresent(this::activate);
                    }
                }

                private void onExtinguished() {
                    if (activationType.compareAndSet(ActivationType.BY_FIRE, ActivationType.NOT_ACTIVATED)) {
                        zombiesPlayer.getPlayer().ifPresent(this::deactivate);
                    }
                }

                private void onPlayerTakeDamage(ZombiesPlayerDamageEvent event) {
                    if (event.getZombiesPlayer() != zombiesPlayer) {
                        return;
                    }

                    Damage damage = event.cause().getDamage();
                    Float amount = data.damageThresholds.get(damage.getTag(DamageUtils.DAMAGE_TYPE_TAG));
                    if (amount == null) {
                        return;
                    }

                    if (damage.getAmount() >= amount && activationType.compareAndSet(ActivationType.NOT_ACTIVATED, ActivationType.BY_DAMAGE)
                        && effectCooldown.takeCooldown(data.duration)) {
                        activate(event.getPlayer());
                    }
                }

                private void activate(Player player) {
                    player.getAttribute(attribute).addModifier(attributeModifier);
                    player.sendMessage(data.activateMessage);
                    player.playSound(data.activateSound);
                }

                private void deactivate(Player player) {
                    player.getAttribute(attribute).removeModifier(attributeModifier);
                    player.sendMessage(data.deactivateMessage);
                    player.playSound(data.deactivateSound);
                }
            }), zombiesPlayer);
        }

        @Override
        public boolean needsTicking() {
            return true;
        }
    }


    @DataObject
    public record Data(Map<String, Float> damageThresholds,
        boolean fireTriggers,
        int duration,
        @NotNull Component activateMessage,
        @NotNull Component deactivateMessage,
        @NotNull Sound activateSound,
        @NotNull Sound deactivateSound,
        @NotNull String attribute,
        float amount,
        AttributeOperation operation) {
    }
}
