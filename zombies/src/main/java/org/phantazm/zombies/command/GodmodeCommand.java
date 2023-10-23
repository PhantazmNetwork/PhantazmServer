package org.phantazm.zombies.command;

import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.Flags;
import org.phantazm.commons.flag.Flaggable;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

public class GodmodeCommand extends SandboxLockedCommand {
    public static final Permission PERMISSION = new Permission("zombies.playtest.godmode");

    public GodmodeCommand() {
        super("godmode", PERMISSION);
    }

    @Override
    protected void runCommand(@NotNull CommandContext context, @NotNull ZombiesScene scene, @NotNull Player sender) {
        scene.setLegit(false);

        ZombiesPlayer zombiesPlayer = scene.managedPlayers().get(PlayerView.lookup(sender.getUuid()));
        Flaggable flags = zombiesPlayer.module().flags();

        boolean res = flags.toggleFlag(Flags.GODMODE);

        if (res) {
            sender.setAllowFlying(true);
            sender.setFlyingSpeed(0.05F);
            sender.sendMessage("Enabled godmode.");
        } else {
            sender.setAllowFlying(false);
            sender.setFlying(false);
            sender.sendMessage("Disabled godmode.");
        }
    }
}
