package org.phantazm.zombies.command;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

public class TestCommand extends SandboxLockedCommand {
    public static final Permission PERMISSION = new Permission("zombies.playtest.test");

    public TestCommand() {
        super("test", PERMISSION);
    }

    @Override
    protected void runCommand(@NotNull CommandContext context, @NotNull ZombiesScene scene, @NotNull Player sender) {
        scene.setLegit(false);

        ZombiesPlayer zombiesPlayer = scene.managedPlayers().get(PlayerView.lookup(sender.getUuid()));
        if (zombiesPlayer == null) {
            return;
        }

        zombiesPlayer.module().getCoins().set(69420666);
        MinecraftServer.getCommandManager().execute(sender, "zombies godmode");
        MinecraftServer.getCommandManager().execute(sender, "zombies round 1");
        MinecraftServer.getCommandManager().execute(sender, "zombies toggle_flag zombies.map.flag.power");
    }
}
