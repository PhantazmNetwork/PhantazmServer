package org.phantazm.zombies.command;

import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.scene2.command.SceneLocalCommand;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Objects;

public abstract class SandboxLockedCommand extends SceneLocalCommand<ZombiesScene> {
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private final Permission permission;

    public SandboxLockedCommand(@NotNull String name, @NotNull Permission permission,
        @NotNull Argument<?>... arguments) {
        super(name, ZombiesScene.class, EMPTY_STRING_ARRAY, arguments);
        this.permission = Objects.requireNonNull(permission);
    }

    @Override
    protected boolean hasPermission(@NotNull CommandContext context, @NotNull ZombiesScene scene, @NotNull Player sender) {
        return sender.hasPermission(permission) || scene.isSandbox();
    }
}
