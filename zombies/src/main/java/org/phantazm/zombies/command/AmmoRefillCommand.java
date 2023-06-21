package org.phantazm.zombies.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.inventory.InventoryAccess;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.core.inventory.InventoryProfile;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class AmmoRefillCommand extends Command {
    public static final Permission PERMISSION = new Permission("zombies.playtest.ammo_refill");

    public AmmoRefillCommand(@NotNull Function<? super UUID, ? extends Optional<ZombiesScene>> sceneMapper) {
        super("ammo_refill");

        setCondition((sender, commandString) -> sender.hasPermission(PERMISSION));
        addConditionalSyntax(getCondition(), (sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("You have to be a player to use that command!", NamedTextColor.RED));
                return;
            }

            UUID uuid = player.getUuid();
            sceneMapper.apply(uuid).ifPresent(scene -> {
                ZombiesPlayer zombiesPlayer = scene.getZombiesPlayers().get(uuid);
                Optional<InventoryAccess> acccessOptional =
                        zombiesPlayer.module().getEquipmentHandler().accessRegistry().getCurrentAccess();

                if (acccessOptional.isPresent()) {
                    InventoryProfile profile = acccessOptional.get().profile();

                    for (int i = 0; i < profile.getSlotCount(); i++) {
                        if (profile.hasInventoryObject(i)) {
                            InventoryObject object = profile.getInventoryObject(i);
                            if (object instanceof Gun gun) {
                                gun.refill();
                            }
                        }
                    }
                }
            });
        });
    }
}
