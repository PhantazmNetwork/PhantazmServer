package org.phantazm.core.player;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minestom.server.utils.mojang.MojangUtils;
import org.jetbrains.annotations.NotNull;
import org.phantazm.stats.Databases;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * An {@link IdentitySource} that calls Mojang's API servers to resolve names and UUIDs. Will log any errors that occur
 * during resolution using its own dedicated {@link Logger}.
 * <p>
 * Username requests may be cached by either {@link Databases#usernames()} and/or {@link MojangUtils}.
 *
 * @apiNote Not part of the public API.
 */
public class MojangIdentitySource implements IdentitySource {
    private static final Logger LOGGER = LoggerFactory.getLogger(MojangIdentitySource.class);
    private static final String NAME_KEY = "name";
    private static final String ID_KEY = "id";

    /**
     * Creates a new instance of this class.
     */
    public MojangIdentitySource() {

    }

    @Override
    public @NotNull CompletableFuture<Optional<String>> getName(@NotNull UUID uuid) {
        return Databases.usernames().cachedUsername(uuid).thenApply(stringOptional -> {
            if (stringOptional.isPresent()) {
                return stringOptional;
            }

            JsonObject response = null;
            try {
                response = MojangUtils.fromUuid(uuid.toString());
            } catch (RuntimeException exception) {
                LOGGER.error("RuntimeException thrown during name resolution of UUID {}: {}", uuid, exception);
            }

            if (response == null) {
                return Optional.empty();
            }


            JsonElement nameElement = response.get(NAME_KEY);
            if (nameElement != null && nameElement.isJsonPrimitive()) {
                JsonPrimitive primitive = nameElement.getAsJsonPrimitive();
                if (primitive.isString()) {
                    String name = primitive.getAsString();
                    Databases.usernames().submitUsername(uuid, name);

                    return Optional.of(name);
                }
            }

            LOGGER.error("Unexpected response when resolving UUID {}: {}", uuid, response);
            return Optional.empty();
        });
    }

    @Override
    public @NotNull CompletableFuture<Optional<UUID>> getUUID(@NotNull String name) {
        return Databases.usernames().cachedUUID(name).thenApply(optionalUuid -> {
            if (optionalUuid.isPresent()) {
                return optionalUuid;
            }

            JsonObject response = null;
            try {
                response = MojangUtils.fromUsername(name);
            } catch (RuntimeException exception) {
                LOGGER.error("RuntimeException thrown during UUID resolution of name {}: {}", name, exception);
            }

            if (response != null) {
                JsonElement idElement = response.get(ID_KEY);
                if (idElement != null && idElement.isJsonPrimitive()) {
                    JsonPrimitive idPrimitive = idElement.getAsJsonPrimitive();
                    if (idPrimitive.isString()) {
                        return Optional.of(UUID.fromString(idPrimitive.getAsString().replaceFirst(
                            "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5")));
                    }
                }

                LOGGER.error("Unexpected response when resolving username {}: {}", name, response);
            }

            return Optional.empty();
        });
    }
}
