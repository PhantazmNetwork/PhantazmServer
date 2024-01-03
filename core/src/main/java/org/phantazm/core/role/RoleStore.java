package org.phantazm.core.role;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface RoleStore {
    String DEFAULT = "default";

    void register(@NotNull Role role);

    @NotNull CompletableFuture<Role> getStylingRole(@NotNull UUID uuid);

    @NotNull Optional<Role> role(@NotNull String identifier);

    @NotNull CompletableFuture<Boolean> giveRole(@NotNull UUID uuid, @NotNull String identifier);

    @NotNull CompletableFuture<Boolean> removeRole(@NotNull UUID uuid, @NotNull String identifier);

    @NotNull CompletableFuture<@Unmodifiable Set<@NotNull Role>> roles(@NotNull UUID uuid);
}
