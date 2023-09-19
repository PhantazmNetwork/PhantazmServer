package org.phantazm.zombies.command;

import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.inventory.InventoryAccess;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.core.inventory.InventoryProfile;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Optional;

public class AmmoRefillCommand extends SandboxLockedCommand {
    public static final Permission PERMISSION = new Permission("zombies.playtest.ammo_refill");

    public AmmoRefillCommand() {
        super("ammo_refill", PERMISSION);
    }

    @Override
    protected void runCommand(@NotNull CommandContext context, @NotNull ZombiesScene scene, @NotNull Player sender) {
        scene.setLegit(false);

        ZombiesPlayer zombiesPlayer = scene.managedPlayers().get(PlayerView.lookup(sender.getUuid()));
        Optional<InventoryAccess> acccessOptional = zombiesPlayer.module().getEquipmentHandler().accessRegistry()
            .getCurrentAccess();
        if (acccessOptional.isEmpty()) {
            return;
        }

        InventoryProfile profile = acccessOptional.get().profile();
        for (int i = 0; i < profile.getSlotCount(); i++) {
            if (!profile.hasInventoryObject(i)) {
                continue;
            }

            InventoryObject object = profile.getInventoryObject(i);
            if (object instanceof Gun gun) {
                gun.refill();
            }
        }
    }
}
