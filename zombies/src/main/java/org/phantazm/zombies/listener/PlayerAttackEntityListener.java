package org.phantazm.zombies.listener;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Tickable;
import org.phantazm.core.equipment.Equipment;
import org.phantazm.core.inventory.InventoryAccessRegistry;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.core.inventory.InventoryProfile;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.zombies.Flags;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class PlayerAttackEntityListener extends ZombiesPlayerEventListener<EntityAttackEvent> implements Tickable {
    private final MobStore mobStore;
    private final float punchDamage;
    private final int punchCooldown;
    private final float punchKnockback;

    private final Tag<Long> lastPunchTicksTag;
    private long ticks = 0;

    public PlayerAttackEntityListener(@NotNull Instance instance,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers, @NotNull MobStore mobStore,
            float punchDamage, int punchCooldown, float punchKnockback) {
        super(instance, zombiesPlayers);
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
        this.punchDamage = punchDamage;
        this.lastPunchTicksTag = Tag.Long("last_punch").defaultValue(0L);
        this.punchCooldown = punchCooldown;
        this.punchKnockback = punchKnockback;
    }

    @Override
    public void tick(long time) {
        ++ticks;
    }

    @Override
    protected void accept(@NotNull ZombiesPlayer zombiesPlayer, @NotNull EntityAttackEvent event) {
        Optional<Player> playerOptional = zombiesPlayer.getPlayer();
        if (playerOptional.isEmpty()) {
            return;
        }

        Player player = playerOptional.get();
        if (player.getInstance() != event.getTarget().getInstance()) {
            //Minestom does not prevent cross-dimensional attacks
            return;
        }

        if (!zombiesPlayer.canUseEquipment()) {
            return;
        }

        Entity target = event.getTarget();
        InventoryAccessRegistry profileSwitcher = zombiesPlayer.module().getInventoryAccessRegistry();
        profileSwitcher.getCurrentAccess().ifPresent(inventoryAccess -> {
            InventoryProfile profile = inventoryAccess.profile();
            int heldSlot = player.getHeldSlot();
            if (!profile.hasInventoryObject(heldSlot)) {
                handleNoEquipmentAttack(zombiesPlayer, player, target);
                return;
            }

            InventoryObject object = profile.getInventoryObject(heldSlot);
            if (!(object instanceof Equipment equipment)) {
                handleNoEquipmentAttack(zombiesPlayer, player, target);
                return;
            }

            equipment.attack(target);
        });
    }

    private void handleNoEquipmentAttack(@NotNull ZombiesPlayer zombiesPlayer, @NotNull Player player,
            @NotNull Entity target) {
        Instance instance = player.getInstance();
        if (instance == null) {
            return;
        }

        if (!zombiesPlayer.canDoGenericActions()) {
            return;
        }

        boolean godmode = zombiesPlayer.flags().hasFlag(Flags.GODMODE);

        long currentTime = 0L;
        if (!godmode && ticks - player.getTag(lastPunchTicksTag) < punchCooldown) {
            return;
        }


        PhantazmMob hit = mobStore.getMob(target.getUuid());
        if (hit == null) {
            return;
        }

        LivingEntity entity = hit.entity();

        if (godmode) {
            entity.kill();
            return;
        }

        double angle = player.getPosition().yaw() * (Math.PI / 180);

        entity.damage(Damage.fromPlayer(player, punchDamage), false);
        entity.takeKnockback(punchKnockback, Math.sin(angle), -Math.cos(angle));
        player.setTag(lastPunchTicksTag, ticks);
    }
}
