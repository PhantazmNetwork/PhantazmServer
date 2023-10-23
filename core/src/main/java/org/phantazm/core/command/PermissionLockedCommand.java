package org.phantazm.core.command;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PermissionLockedCommand extends Command {
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private final Permission permission;

    public PermissionLockedCommand(@NotNull String name, @NotNull Permission permission, @Nullable String... aliases) {
        super(name, aliases);
        this.permission = Objects.requireNonNull(permission);
        this.setCondition(this::checkPermission);
    }

    public PermissionLockedCommand(@NotNull String name, @NotNull Permission permission) {
        this(name, permission, EMPTY_STRING_ARRAY);
    }

    protected boolean checkPermission(CommandSender sender, String command) {
        return sender.hasPermission(permission);
    }
}
