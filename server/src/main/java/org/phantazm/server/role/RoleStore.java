package org.phantazm.server.role;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface RoleStore {
    void register(@NotNull Role role);

    @NotNull CompletableFuture<Role> getStylingRole(@NotNull Player player);

    void applyRoles(@NotNull Player player);

    @NotNull Optional<Role> role(@NotNull String identifier);

    void giveRole(@NotNull UUID uuid, @NotNull String identifier);

    void removeRole(@NotNull UUID uuid, @NotNull String identifier);
}
