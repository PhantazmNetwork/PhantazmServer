package org.phantazm.zombies.command;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.command.CommandUtils;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.scene2.SceneManager;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.scene2.ZombiesScene;

public class KillAllCommand extends SandboxLockedCommand {
    public static final Permission PERMISSION = new Permission("zombies.playtest.killall");

    public KillAllCommand(@NotNull PlayerViewProvider viewProvider) {
        super("killall", PERMISSION);
        addConditionalSyntax(CommandUtils.playerSenderCondition(), (sender, context) -> {
            Player playerSender = (Player) sender;
            PlayerView playerView = viewProvider.fromPlayer(playerSender);
            SceneManager.Global.instance().currentScene(playerView, ZombiesScene.class).ifPresent(scene -> {
                if (super.cannotExecute(sender, scene)) {
                    return;
                }

                scene.getAcquirable().sync(self -> {
                    self.setLegit(false);

                    self.map().roundHandler().currentRound().ifPresent(round -> {
                        for (Mob mob : round.getSpawnedMobs()) {
                            mob.getAcquirable().sync(self1 -> {
                                ((LivingEntity) self1).damage(Damage.fromPlayer(playerSender, mob.getHealth()), true);
                            });
                        }
                    });
                });
            });
        });
    }
}
