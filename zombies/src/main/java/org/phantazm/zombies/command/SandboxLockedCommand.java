package org.phantazm.zombies.command;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Objects;

public class SandboxLockedCommand extends Command {
    private final Permission permission;

    public SandboxLockedCommand(@NotNull String name, @NotNull Permission permission) {
        super(name);
        this.permission = Objects.requireNonNull(permission);
    }

    protected boolean cannotExecute(@NotNull CommandSender sender, @NotNull ZombiesScene scene) {
        return !sender.hasPermission(permission) && !scene.isSandbox();
    }
}
