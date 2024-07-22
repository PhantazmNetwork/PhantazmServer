package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.EventListener;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.DamageUtils;
import org.phantazm.core.tick.Activable;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.event.player.ZombiesPlayerDamageEvent;
import org.phantazm.zombies.event.player.ZombiesPlayerMeleeEntityEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;

@Model("zombies.upgrade.life_link")
@Cache
public class LifeLinkUpgrade implements PlayerUpgradeComponent {
    private final Data data;

    @FactoryMethod
    public LifeLinkUpgrade(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull PlayerUpgrade apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer player) {
        return new Internal(player, data);
    }

    private static class Internal extends GuardedPlayerUpgrade {
        private Internal(ZombiesPlayer zombiesPlayer, Data data) {
            super(Activable.threadsafeWrapper(new Activable() {
                private final EventListener<ZombiesPlayerMeleeEntityEvent> melee = EventListener
                    .builder(ZombiesPlayerMeleeEntityEvent.class).handler(this::meleeEvent).build();

                private final EventListener<ZombiesPlayerDamageEvent> damage = EventListener
                    .builder(ZombiesPlayerDamageEvent.class).handler(this::damageEvent).build();

                private final AtomicReference<Reference<LivingEntity>> target =
                    new AtomicReference<>(new WeakReference<>(null));

                @Override
                public void start() {
                    zombiesPlayer.getScene().sceneNode().addListener(melee);
                    zombiesPlayer.getScene().sceneNode().addListener(damage);
                }

                @Override
                public void end() {
                    zombiesPlayer.getScene().sceneNode().removeListener(melee);
                    zombiesPlayer.getScene().sceneNode().removeListener(damage);
                }

                private void damageEvent(ZombiesPlayerDamageEvent event) {
                    if (event.zombiesPlayer() != zombiesPlayer) {
                        return;
                    }

                    LivingEntity entity = target.get().get();
                    if (entity == null) {
                        return;
                    }

                    double transferFactor;
                    Entity attacker = event.cause().getDamage().getAttacker();
                    if (attacker instanceof Mob mob &&
                        mob.data().tags().contains(data.specialTag)) {
                        transferFactor = data.specialFactor;
                    } else {
                        transferFactor = data.normalFactor;
                    }

                    if (attacker instanceof LivingEntity livingAttacker) {
                        livingAttacker.getAcquirable().sync(self -> {
                            DamageUtils.damage(((LivingEntity) self), event.getPlayer(),
                                (float) (transferFactor * event.cause().getDamage().getAmount()), true);
                        });
                    }

                    Damage oldDamage = event.cause().getDamage();
                    event.cause().setDamage(new Damage(oldDamage.getType(), oldDamage.getSource(),
                        oldDamage.getAttacker(), oldDamage.getSourcePosition(),
                        (float) (oldDamage.getAmount() * (1 - transferFactor))));
                }

                private void meleeEvent(ZombiesPlayerMeleeEntityEvent event) {
                    if (event.zombiesPlayer() != zombiesPlayer) {
                        return;
                    }

                    Reference<LivingEntity> entityReference = target.get();
                    LivingEntity target = event.target();
                    if (entityReference.refersTo(target)) {
                        return;
                    }

                    this.target.set(new WeakReference<>(target));
                }
            }), zombiesPlayer);
        }

        @Override
        public boolean needsTicking() {
            return false;
        }
    }


    @DataObject
    public record Data(double normalFactor,
        double specialFactor,
        @NotNull Key specialTag) {

    }
}
