package org.phantazm.zombies.equipment.perk.equipment.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.annotation.document.Description;
import com.github.steanky.toolkit.collection.Wrapper;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.RayUtils;
import org.phantazm.mob.MobStore;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;

@Description("""
        Interactor capable of hitting a mob in a Zombies game. Supports variable reach, knockback, and damage.
        """)
@Model("zombies.perk.interactor.melee")
@Cache(false)
public class MeleeInteractorCreator implements PerkInteractorCreator {
    private final Data data;
    private final MobStore mobStore;

    @FactoryMethod
    public MeleeInteractorCreator(@NotNull Data data, @NotNull MobStore mobStore) {
        this.data = Objects.requireNonNull(data, "data");
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
    }

    @Override
    public @NotNull PerkInteractor forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        return new Interactor(data, zombiesPlayer, mobStore);
    }

    private static class Interactor implements PerkInteractor {
        private final Data data;
        private final ZombiesPlayer zombiesPlayer;
        private final MobStore mobStore;

        private Interactor(@NotNull Data data, @NotNull ZombiesPlayer zombiesPlayer, @NotNull MobStore mobStore) {
            this.data = Objects.requireNonNull(data, "data");
            this.zombiesPlayer = Objects.requireNonNull(zombiesPlayer, "zombiesPlayer");
            this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
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
                                Pos hitPosition = hit.getPosition();

                                RayUtils.rayTrace(boundingBox, hitPosition, eyePos).ifPresent(vec -> {
                                    HitResult closestHit = closest.get();
                                    if ((closestHit == null ||
                                            vec.distanceSquared(eyePos) < closestHit.pos.distanceSquared(eyePos)) &&
                                            CollisionUtils.isLineOfSightReachingShape(instance, player.getChunk(),
                                                    eyePos, hitPosition, boundingBox)) {
                                        closest.set(new HitResult(hit, vec));
                                    }
                                });
                            }
                        });

                HitResult hit = closest.get();
                if (hit == null || hit.pos.distanceSquared(eyePos) > data.reach * data.reach) {
                    return false;
                }

                double angle = feetPos.yaw() * (Math.PI / 180);
                hit.entity.damage(DamageType.fromPlayer(player), data.damage, data.bypassArmor);
                hit.entity.takeKnockback(0.4F * data.knockback, Math.sin(angle), -Math.cos(angle));
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
                       @Description(
                               "The amount of knockback the weapon deals; 1 is the vanilla knockback from an unarmed " +
                                       "hand") float knockback,
                       @Description("Whether damage from this weapon should bypass enemy armor") boolean bypassArmor) {
    }

    private record HitResult(LivingEntity entity, Vec pos) {

    }
}
