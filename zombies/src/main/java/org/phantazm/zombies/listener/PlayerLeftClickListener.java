package org.phantazm.zombies.listener;

import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.RayUtils;
import org.phantazm.core.equipment.Equipment;
import org.phantazm.core.inventory.InventoryAccessRegistry;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.core.inventory.InventoryProfile;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.zombies.Flags;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.*;
import java.util.function.Consumer;

public class PlayerLeftClickListener extends ZombiesPlayerEventListener<PlayerHandAnimationEvent> {
    private final MobStore mobStore;
    private final float punchDamage;
    private final float punchRange;
    private final int punchCooldown;
    private final float punchKnockback;

    private final Tag<Long> lastPunchTag;

    public PlayerLeftClickListener(@NotNull Instance instance,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers, @NotNull MobStore mobStore,
            float punchDamage, float punchRange, int punchCooldown, float punchKnockback) {
        super(instance, zombiesPlayers);
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
        this.punchDamage = punchDamage;
        this.punchRange = punchRange;
        this.lastPunchTag = Tag.Long("last_punch").defaultValue(0L);
        this.punchCooldown = punchCooldown;
        this.punchKnockback = punchKnockback;
    }

    @Override
    protected void accept(@NotNull ZombiesPlayer zombiesPlayer, @NotNull PlayerHandAnimationEvent event) {
        if (event.getHand() != Player.Hand.MAIN) {
            return;
        }

        if (!zombiesPlayer.canUseEquipment()) {
            return;
        }

        InventoryAccessRegistry profileSwitcher = zombiesPlayer.module().getInventoryAccessRegistry();
        profileSwitcher.getCurrentAccess().ifPresent(inventoryAccess -> {
            InventoryProfile profile = inventoryAccess.profile();
            if (!profile.hasInventoryObject(event.getPlayer().getHeldSlot())) {
                handleNoEquipmentLeftClick(zombiesPlayer);
                return;
            }

            InventoryObject object = profile.getInventoryObject(event.getPlayer().getHeldSlot());
            if (!(object instanceof Equipment equipment)) {
                handleNoEquipmentLeftClick(zombiesPlayer);
                return;
            }

            equipment.leftClick();
        });
    }

    private void handleNoEquipmentLeftClick(@NotNull ZombiesPlayer zombiesPlayer) {
        zombiesPlayer.getPlayer().ifPresent(player -> {
            Instance instance = player.getInstance();
            assert instance != null;

            if (!zombiesPlayer.canDoGenericActions()) {
                return;
            }

            boolean godmode = zombiesPlayer.flags().hasFlag(Flags.GODMODE);

            long currentTime = 0L;
            if (!godmode && ((currentTime = System.currentTimeMillis()) - player.getTag(lastPunchTag)) /
                    MinecraftServer.TICK_MS < punchCooldown) {
                return;
            }

            Pos start = player.getPosition().add(0, player.getEyeHeight(), 0);
            Vec end = start.direction().mul(godmode ? 20 : punchRange);

            ClosestHit closestHit = new ClosestHit(start);
            instance.getEntityTracker()
                    .raytraceCandidates(start, end, EntityTracker.Target.LIVING_ENTITIES, closestHit);

            if (closestHit.closest > punchRange * punchRange) {
                return;
            }

            PhantazmMob hit = closestHit.closestMob;
            if (hit != null) {
                LivingEntity entity = hit.entity();

                if (godmode) {
                    entity.kill();
                }
                else {
                    double angle = player.getPosition().yaw() * (Math.PI / 180);

                    entity.damage(Damage.fromPlayer(player, punchDamage), false);
                    entity.takeKnockback(punchKnockback, Math.sin(angle), -Math.cos(angle));
                    player.setTag(lastPunchTag, currentTime);
                }
            }
        });
    }

    private class ClosestHit implements Consumer<LivingEntity> {
        private final Pos start;

        private PhantazmMob closestMob;
        private double closest = Double.POSITIVE_INFINITY;

        private ClosestHit(Pos start) {
            this.start = start;
        }

        @Override
        public void accept(LivingEntity candidate) {
            PhantazmMob mob = mobStore.getMob(candidate.getUuid());
            if (mob == null) {
                return;
            }

            BoundingBox boundingBox = candidate.getBoundingBox();
            RayUtils.rayTrace(boundingBox, candidate.getPosition(), start).ifPresent(vec -> {
                double thisDistance = vec.distanceSquared(start);
                if (thisDistance < closest) {
                    this.closestMob = mob;
                    this.closest = thisDistance;
                }
            });
        }
    }
}
