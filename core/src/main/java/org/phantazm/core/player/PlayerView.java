package org.phantazm.core.player;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.tag.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;

/**
 * Represents a view of a player that may be offline. Provides their UUID as well as a means to access the
 * {@link Player} instance associated with said UUID.
 * <p>
 * This interface extends {@link Taggable}, through which arbitrary data may be associated with this PlayerView. Note
 * that this data is <i>persistent</i> so long as the player is online, or a reference to this PlayerView is maintained.
 * As a matter of convention, the tags should be persistent and not cleared even when the player transfers between
 * scenes or disconnects.
 *
 * @implSpec Implementations should anticipate many calls to {@code getPlayer}, and are encouraged to perform caching if
 * necessary to improve performance. However, since PlayerView instances are intended for long-term storage in fields
 * and other places where they may become out-of-date, it is important to avoid keeping strong references to any cached
 * Player objects within the PlayerView implementation itself.
 * <p>
 * Implementations must also ensure an {@link Object#equals(Object)} and {@link Object#hashCode()} implementation based
 * only on equality checking or hashing the {@link UUID} returned by calling {@link PlayerView#getUUID()}.
 * @see PlayerViewImpl
 */
public sealed interface PlayerView extends Taggable permits PlayerViewImpl, PlayerView.Lookup {
    /**
     * Creates a new "lookup" PlayerView instance. Useful when, given a UUID, is it necessary to determine if a given
     * set of PlayerView objects contains a player whose UUID matches a known UUID. The UUID need not correspond to a
     * valid Minecraft user.
     * <p>
     * The returned object will be unbound to any particular {@link IdentitySource}. Therefore, the player's username
     * (and consequently their display name) will always be their UUID. Likewise, the PlayerView is not bound to an
     * existing player, online or otherwise, and therefore its {@link PlayerView#getPlayer()} method will always return
     * an empty optional.
     * <p>
     * For obtaining instances of PlayerView that <i>are</i> bound to an IdentitySource and do correspond to an actual
     * account, see {@link PlayerViewProvider}.
     * <p>
     * <b>Unless you know what you're doing, you probably want to get your PlayerView instances from PlayerViewProvider
     * instead of this method!</b>
     *
     * @param uuid the UUID from which to create a new lookup PlayerView
     * @return a new PlayerView useful for map or set lookups
     */
    static @NotNull PlayerView lookup(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid);
        return new Lookup(uuid);
    }

    /**
     * Given some collection of PlayerView objects, populates a collection returned by {@code creator} containing the
     * existing {@link Player} objects contained in the optionals returned by calling {@link PlayerView#getPlayer()} on
     * each element of {@code input}.
     *
     * @param input   the input collection
     * @param creator the creator of the output collection, which accepts the size of the input collection as an
     *                argument
     * @param <T>     the input type
     * @param <V>     the output type
     * @return the collection returned by {@code creator}, populated with Player objects
     */
    static <T extends Collection<Player>,
        V extends Collection<? extends PlayerView>> @NotNull T getMany(@NotNull V input, @NotNull IntFunction<? extends T> creator) {
        T out = creator.apply(input.size());
        for (PlayerView view : input) {
            view.getPlayer().ifPresent(out::add);
        }

        return out;
    }

    /**
     * A special {@link PlayerView} implementation that is unbound to a player instance, but can be created using only a
     * {@link UUID}. Instances can be obtained by calling {@link PlayerView#lookup(UUID)}.
     */
    final class Lookup implements PlayerView {
        private final UUID uuid;
        private final int hashCode;

        private final TagHandler tagHandler;

        private Lookup(UUID uuid) {
            this.uuid = uuid;
            this.hashCode = uuid.hashCode();
            this.tagHandler = TagHandler.newHandler();
        }

        @Override
        public @NotNull UUID getUUID() {
            return uuid;
        }

        @Override
        public @NotNull CompletableFuture<String> getUsername() {
            return CompletableFuture.completedFuture(uuid.toString());
        }

        @Override
        public @NotNull Optional<String> getUsernameIfCached() {
            return Optional.empty();
        }

        @Override
        public @NotNull CompletableFuture<Component> getDisplayName() {
            return CompletableFuture.completedFuture(Component.text(uuid.toString()));
        }

        @Override
        public @NotNull Optional<Component> getDisplayNameIfCached() {
            return Optional.empty();
        }

        @Override
        public @NotNull Optional<Player> getPlayer() {
            return Optional.empty();
        }

        @Override
        public @NotNull TagHandler tagHandler() {
            return tagHandler;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (obj == this) {
                return true;
            }

            if (obj instanceof PlayerView other) {
                return uuid.equals(other.getUUID());
            }

            return false;
        }

        @Override
        public String toString() {
            return "PlayerViewLookup[uuid=" + uuid + "]";
        }
    }

    /**
     * Gets the {@link UUID} of the player.
     *
     * @return The {@link UUID} of the player
     */
    @NotNull UUID getUUID();

    /**
     * Attempts to resolve the username of this player. May perform an IO operation to determine the name from the UUID,
     * if necessary, and should cache the results of this operation.
     *
     * @return a {@link CompletableFuture} representing an attempt at retrieving the username of this player. If the
     * username cannot be found due to network conditions, an invalid UUID, or this instance is not bound to a specific
     * {@link IdentitySource}, the returned String will be the result of calling {@link UUID#toString()} on the stored
     * UUID
     */
    @NotNull CompletableFuture<String> getUsername();

    /**
     * Retrieves the player's username immediately if it is cached in this object, or the player is currently online.
     * May be out-of-date, as it is not defined when (if ever) a previously cached username becomes invalid.
     *
     * @return an Optional containing the cached username, or an empty Optional if not present
     */
    @NotNull Optional<String> getUsernameIfCached();

    /**
     * Asynchronously gets the display name of this player. If they are currently online, this function will immediately
     * exit with a completed {@link CompletableFuture} containing the result of calling {@link Player#getDisplayName()}.
     * Otherwise, the player's username will be resolved given their UUID (which may entail a request to Mojang's API
     * servers) and a plain text component (with no styling applied) containing the player's username will, when the
     * operation completes, be set as the future's value.
     *
     * @return a CompletableFuture containing the player's current display name
     */
    @NotNull CompletableFuture<Component> getDisplayName();

    /**
     * Immediately gets the display name of this player if it is cached in this object, or if the player is currently
     * online. May be out-of-date, as it is not defined when (if ever) a previously cached display name becomes
     * invalid.
     *
     * @return an Optional containing the cached username, or an empty Optional if not present
     */
    @NotNull Optional<Component> getDisplayNameIfCached();

    /**
     * Gets an {@link Optional} which may contain the player, only if they are online. Maintaining strong references to
     * {@link Player} objects should be avoided.
     *
     * @return An {@link Optional} of the player which is empty when the player is offline
     */
    @NotNull Optional<Player> getPlayer();
}
