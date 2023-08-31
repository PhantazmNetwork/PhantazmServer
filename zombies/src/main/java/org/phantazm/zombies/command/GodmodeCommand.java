package org.phantazm.zombies.command;

import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.command.CommandUtils;
import org.phantazm.core.command.PermissionLockedCommand;
import org.phantazm.zombies.Flags;
import org.phantazm.zombies.map.Flaggable;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class GodmodeCommand extends PermissionLockedCommand {
    public static final Permission PERMISSION = new Permission("zombies.playtest.godmode");

    public GodmodeCommand(@NotNull Function<? super UUID, Optional<ZombiesScene>> sceneMapper) {
        super("godmode", PERMISSION);

        addConditionalSyntax(CommandUtils.playerSenderCondition(), (sender, context) -> {
            Player player = (Player) sender;
            UUID uuid = player.getUuid();
            sceneMapper.apply(uuid).ifPresent(scene -> {
                scene.setLegit(false);

                ZombiesPlayer zombiesPlayer = scene.getZombiesPlayers().get(uuid);
                Flaggable flags = zombiesPlayer.module().flags();

                boolean res = flags.toggleFlag(Flags.GODMODE);
                if (res) {
                    player.setAllowFlying(true);
                    player.setFlyingSpeed(0.05F);
                    player.sendMessage("Enabled godmode.");
                } else {
                    player.setAllowFlying(false);
                    player.setFlying(false);
                    player.sendMessage("Disabled godmode.");
                }
            });
        });
    }
}
