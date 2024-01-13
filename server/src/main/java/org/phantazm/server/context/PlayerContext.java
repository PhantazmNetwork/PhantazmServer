package org.phantazm.server.context;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.role.RoleStore;
import org.phantazm.server.permission.PermissionHandler;
import org.phantazm.server.validator.LoginValidator;

import java.util.Map;
import java.util.UUID;

public record PlayerContext(@NotNull LoginValidator loginValiator,
    @NotNull PermissionHandler permissionHandler,
    @NotNull PlayerViewProvider playerViewProvider,
    @NotNull RoleStore roles,
    @NotNull Map<? super UUID, ? extends Party> parties) {
}
