package org.phantazm.zombies.command;

import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.command.CommandUtils;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.scene2.SceneManager;
import org.phantazm.zombies.Flags;
import org.phantazm.commons.flag.Flaggable;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

public class GodmodeCommand extends SandboxLockedCommand {
    public static final Permission PERMISSION = new Permission("zombies.playtest.godmode");

    public GodmodeCommand(@NotNull PlayerViewProvider viewProvider) {
        super("godmode", PERMISSION);

        addConditionalSyntax(CommandUtils.playerSenderCondition(), (sender, context) -> {
            Player senderPlayer = (Player) sender;

            PlayerView playerView = viewProvider.fromPlayer(senderPlayer);
            SceneManager.Global.instance().currentScene(playerView, ZombiesScene.class).ifPresent(scene -> {
                if (super.cannotExecute(sender, scene)) {
                    return;
                }

                scene.getAcquirable().sync(self -> {
                    self.setLegit(false);

                    ZombiesPlayer zombiesPlayer = self.managedPlayers().get(playerView);
                    Flaggable flags = zombiesPlayer.module().flags();

                    boolean res = flags.toggleFlag(Flags.GODMODE);

                    if (res) {
                        senderPlayer.setAllowFlying(true);
                        senderPlayer.setFlyingSpeed(0.05F);
                        senderPlayer.sendMessage("Enabled godmode.");
                    } else {
                        senderPlayer.setAllowFlying(false);
                        senderPlayer.setFlying(false);
                        senderPlayer.sendMessage("Disabled godmode.");
                    }
                });
            });
        });
    }
}
