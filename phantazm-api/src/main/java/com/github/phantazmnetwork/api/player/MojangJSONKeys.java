package com.github.phantazmnetwork.api.player;
import com.google.gson.JsonObject;

/**
 * Contains string constants corresponding to keys used to access data in {@link JsonObject} instances returned by the
 * Mojang API. Not part of the public API.
 */
final class MojangJSONKeys {
    private MojangJSONKeys() { throw new UnsupportedOperationException(); }

    /**
     * The player name key, used to retrieve a player's human-readable name.
     */
    static final String PLAYER_NAME = "name";

    /**
     * The player ID key, used to retrieve a player's UUID.
     */
    static final String PLAYER_ID = "id";
}
