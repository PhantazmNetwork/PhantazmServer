package org.phantazm.zombies.equipment.perk.equipment.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.annotation.document.Description;
import com.github.steanky.toolkit.collection.Wrapper;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.RayUtils;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.zombies.ExtraNodeKeys;
import org.phantazm.zombies.Flags;
import org.phantazm.zombies.coin.ModifierSourceGroups;
import org.phantazm.zombies.coin.PlayerCoins;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.map.Flaggable;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Collection;
import java.util.Objects;

@Description("""
        Interactor capable of hitting a mob in a Zombies game. Supports variable reach, knockback, and damage.
        """)
@Model("zombies.perk.interactor.melee")
@Cache(false)
public class MeleeInteractorCreator implements PerkInteractorCreator {
    private final Data data;
    private final MobStore mobStore;
    private final Flaggable mapFlags;

    @FactoryMethod
    public MeleeInteractorCreator(@NotNull Data data, @NotNull MobStore mobStore, @NotNull MapObjects mapObjects) {
        this.data = Objects.requireNonNull(data, "data");
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
        this.mapFlags = mapObjects.module().flags();
    }

    @Override
    public @NotNull PerkInteractor forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        return new Interactor(data, zombiesPlayer, mobStore, mapFlags);
    }

    private static class Interactor implements PerkInteractor {
        private final Data data;
        private final ZombiesPlayer zombiesPlayer;
        private final MobStore mobStore;
        private final Flaggable mapFlags;

        private Interactor(@NotNull Data data, @NotNull ZombiesPlayer zombiesPlayer, @NotNull MobStore mobStore,
                Flaggable mapFlags) {
            this.data = Objects.requireNonNull(data, "data");
            this.zombiesPlayer = Objects.requireNonNull(zombiesPlayer, "zombiesPlayer");
            this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
            this.mapFlags = mapFlags;
        }

        @Override
        public boolean setSelected(boolean selected) {
            return false;
        }

        @Override
        public boolean leftClick() {
            return zombiesPlayer.getPlayer().map(player -> {
                Instance instance = player.getInstance();
                if (instance == null) {
                    return false;
                }

                Pos feetPos = player.getPosition();
                Pos eyePos = feetPos.add(0, player.getEyeHeight(), 0);
                Point targetPos = eyePos.add(feetPos.direction().mul(data.reach));

                Wrapper<HitResult> closest = Wrapper.ofNull();
                instance.getEntityTracker()
                        .raytraceCandidates(eyePos, targetPos, EntityTracker.Target.LIVING_ENTITIES, hit -> {
                            if (mobStore.hasMob(hit.getUuid())) {
                                BoundingBox boundingBox = hit.getBoundingBox();

                                RayUtils.rayTrace(boundingBox, hit.getPosition(), eyePos).ifPresent(vec -> {
                                    HitResult closestHit = closest.get();
                                    if ((closestHit == null ||
                                            vec.distanceSquared(eyePos) < closestHit.pos.distanceSquared(eyePos)) &&
                                            player.hasLineOfSight(hit)) {
                                        closest.set(new HitResult(hit, vec));
                                    }
                                });
                            }
                        });

                HitResult hit = closest.get();
                if (hit == null || hit.pos.distanceSquared(eyePos) > data.reach * data.reach) {
                    return false;
                }

                PhantazmMob hitMob = mobStore.getMob(hit.entity().getUuid());
                if ((mapFlags.hasFlag(Flags.INSTA_KILL) || zombiesPlayer.flags().hasFlag(Flags.INSTA_KILL)) &&
                        (hitMob != null && !hitMob.model().getExtraNode()
                                .getBooleanOrDefault(false, ExtraNodeKeys.RESIST_INSTAKILL))) {
                    hit.entity.kill();
                }
                else {
                    double angle = feetPos.yaw() * (Math.PI / 180);
                    hit.entity.damage(Damage.fromPlayer(player, data.damage), data.bypassArmor);
                    hit.entity.takeKnockback(data.knockback, Math.sin(angle), -Math.cos(angle));
                }

                PlayerCoins coins = zombiesPlayer.module().getCoins();
                Collection<Transaction.Modifier> modifiers = zombiesPlayer.module().compositeTransactionModifiers()
                        .modifiers(ModifierSourceGroups.MOB_COIN_GAIN);

                coins.runTransaction(new Transaction(modifiers, data.coins)).applyIfAffordable(coins);

                return true;
            }).orElse(false);
        }

        @Override
        public boolean rightClick() {
            return false;
        }
    }

    @DataObject
    public record Data(@Description("The reach of this weapon") double reach,
                       @Description("The damage it does on a successful hit") float damage,
                       @Description("The amount of knockback the weapon deals; 0.4 is the vanilla knockback from an " +
                               "unarmed hand") float knockback,
                       @Description("The number of coins to give on a successful hit.") int coins,
                       @Description("Whether damage from this weapon should bypass enemy armor") boolean bypassArmor) {
    }

    private record HitResult(LivingEntity entity, Vec pos) {

    }
}
