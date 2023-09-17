package org.phantazm.zombies.command;

import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.command.CommandUtils;
import org.phantazm.core.inventory.InventoryAccess;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.core.inventory.InventoryProfile;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.scene2.SceneManager;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Optional;

public class AmmoRefillCommand extends SandboxCommand {
    public static final Permission PERMISSION = new Permission("zombies.playtest.ammo_refill");

    public AmmoRefillCommand(@NotNull PlayerViewProvider viewProvider) {
        super("ammo_refill", PERMISSION);

        addConditionalSyntax(CommandUtils.playerSenderCondition(), (sender, context) -> {
            PlayerView playerView = viewProvider.fromPlayer((Player) sender);
            SceneManager.Global.instance().currentScene(playerView, ZombiesScene.class).ifPresent(scene -> {
                if (super.cannotExecute(sender, scene)) {
                    return;
                }

                scene.getAcquirable().sync(self -> {
                    self.setLegit(false);

                    ZombiesPlayer zombiesPlayer = scene.managedPlayers().get(playerView);
                    Optional<InventoryAccess> acccessOptional =
                        zombiesPlayer.module().getEquipmentHandler().accessRegistry().getCurrentAccess();
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
                });
            });
        });
    }
}
