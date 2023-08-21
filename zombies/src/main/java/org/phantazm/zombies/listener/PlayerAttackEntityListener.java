package org.phantazm.zombies.listener;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.Equipment;
import org.phantazm.core.inventory.InventoryAccessRegistry;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.core.inventory.InventoryProfile;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.Flags;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PlayerAttackEntityListener extends ZombiesPlayerEventListener<EntityAttackEvent> {
    private final float punchDamage;
    private final int punchCooldown;
    private final float punchKnockback;

    private final Tag<Integer> lastPunchTicksTag;

    public PlayerAttackEntityListener(@NotNull Instance instance,
        @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers, float punchDamage, int punchCooldown,
        float punchKnockback) {
        super(instance, zombiesPlayers);
        this.punchDamage = punchDamage;
        this.lastPunchTicksTag = Tag.Integer("last_punch").defaultValue(0);
        this.punchCooldown = punchCooldown;
        this.punchKnockback = punchKnockback;
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

        int currentTick = MinecraftServer.currentTick();
        if (!godmode && currentTick - player.getTag(lastPunchTicksTag) < punchCooldown) {
            return;
        }

        if (!(target instanceof Mob hit)) {
            return;
        }

        if (godmode) {
            hit.kill();
            return;
        }

        double angle = player.getPosition().yaw() * (Math.PI / 180);

        hit.damage(Damage.fromPlayer(player, punchDamage), false);
        hit.takeKnockback(punchKnockback, Math.sin(angle), -Math.cos(angle));
        player.setTag(lastPunchTicksTag, currentTick);
    }
}
