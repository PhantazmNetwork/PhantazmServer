package com.github.phantazmnetwork.api.player;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minestom.server.utils.mojang.MojangUtils;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.UUID;

/**
 * An {@link IdentitySource} that calls Mojang's API servers to resolve names and UUIDs. Not part of the public API.
 *
 * @apiNote Currently, this utilizes {@link MojangUtils} utility methods and does not perform any caching of its own,
 * aside from that utilized by MojangUtils itself.
 */
@Blocking
class MojangIdentitySource implements IdentitySource {
    private static final String NAME_KEY = "name";
    private static final String ID_KEY = "id";

    @Override
    public @NotNull Optional<String> getName(@NotNull UUID uuid) {
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
    }

    @Override
    public @NotNull Optional<UUID> getUUID(@NotNull String name) {
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
    }
}
