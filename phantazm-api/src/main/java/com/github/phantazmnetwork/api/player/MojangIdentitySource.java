package com.github.phantazmnetwork.api.player;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minestom.server.utils.mojang.MojangUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * An {@link IdentitySource} that calls Mojang's API servers to resolve names and UUIDs. Not part of the public API.
 *
 * @apiNote Currently, this utilizes {@link MojangUtils} utility methods and does not perform any caching of its own,
 * aside from that utilized by MojangUtils itself.
 */
@SuppressWarnings("ClassCanBeRecord")
class MojangIdentitySource implements IdentitySource {
    private static final String NAME_KEY = "name";
    private static final String ID_KEY = "id";

    private final Executor executor;

    MojangIdentitySource(@NotNull Executor executor) {
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    @Override
    public @NotNull CompletableFuture<Optional<String>> getName(@NotNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            JsonObject response = MojangUtils.fromUuid(uuid.toString());
            if(response != null) {
                JsonElement nameElement = response.get(NAME_KEY);
                if(nameElement != null && nameElement.isJsonPrimitive()) {
                    JsonPrimitive primitive = nameElement.getAsJsonPrimitive();
                    if(primitive.isString()) {
                        return Optional.of(primitive.getAsString());
                    }
                }
            }

            return Optional.empty();
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Optional<UUID>> getUUID(@NotNull String name) {
        return CompletableFuture.supplyAsync(() -> {
            JsonObject object = MojangUtils.fromUsername(name);
            if(object != null) {
                JsonElement id = object.get(ID_KEY);
                if(id != null && id.isJsonPrimitive()) {
                    JsonPrimitive idPrimitive = id.getAsJsonPrimitive();
                    if(idPrimitive.isString()) {
                        return Optional.of(UUID.fromString(idPrimitive.getAsString()));
                    }
                }
            }

            return Optional.empty();
        }, executor);
    }
}
