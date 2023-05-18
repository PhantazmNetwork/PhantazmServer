package org.phantazm.core.player;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * <p>Basic implementation of a {@link PlayerView}. Caches the player object, so {@code getPlayer} is safe to call
 * frequently. Additionally, these objects are safe for long-term storage because they do not store a strong reference
 * to the player.</p>
 *
 * <p>This class is not part of the public API. Instances may be obtained through a suitable {@link PlayerViewProvider}
 * implementation.</p>
 *
 * @apiNote This class does not provide an implementation of equals/hashCode because of the inherently volatile nature
 * of its internal state (the cached player reference may be garbage collected at any time). {@code getUUID} should be
 * used if a map-safe, representative object is desired.
 * @see Player
 * @see BasicPlayerViewProvider
 */
class BasicPlayerView implements PlayerView {
    private static final Reference<Player> NULL_REFERENCE = new WeakReference<>(null);

    private final IdentitySource identitySource;
    private final ConnectionManager connectionManager;
    private final UUID uuid;

    private final Object usernameLock = new Object();
    private final Object usernameRequestLock = new Object();

    private volatile Reference<Player> playerReference;

    private volatile CompletableFuture<String> usernameRequest;
    private volatile String username;

    /**
     * Creates a basic {@link PlayerView}.
     *
     * @param identitySource    The {@link IdentitySource} instance used to resolve usernames when necessary
     * @param connectionManager The {@link ConnectionManager} used to find {@link Player}s based on their {@link UUID}
     * @param uuid              The {@link UUID} of the {@link Player} to store
     */
    BasicPlayerView(@NotNull IdentitySource identitySource, @NotNull ConnectionManager connectionManager,
            @NotNull UUID uuid) {
        this.identitySource = Objects.requireNonNull(identitySource, "identitySource");
        this.connectionManager = Objects.requireNonNull(connectionManager, "connectionManager");
        this.uuid = Objects.requireNonNull(uuid, "player");
        this.playerReference = new WeakReference<>(null);
    }

    /**
     * Creates a basic {@link PlayerView} given a {@link ConnectionManager} and an already-existing {@link Player}.
     *
     * @param identitySource    The {@link IdentitySource} instance used to resolve usernames when necessary
     * @param connectionManager the ConnectionManager used to find {@link Player}s based on their {@link UUID}
     * @param player            the player, whose UUID and username will be cached immediately
     */
    BasicPlayerView(@NotNull IdentitySource identitySource, @NotNull ConnectionManager connectionManager,
            @NotNull Player player) {
        this.identitySource = Objects.requireNonNull(identitySource, "identitySource");
        this.connectionManager = Objects.requireNonNull(connectionManager, "connectionManager");
        this.uuid = player.getUuid();
        this.playerReference = new WeakReference<>(player);
        this.username = player.getUsername();
    }

    private CompletableFuture<String> getUsernameRequest() {
        synchronized (usernameRequestLock) {
            if (usernameRequest != null) {
                return usernameRequest;
            }

            return usernameRequest = identitySource.getName(uuid).thenApply(nameOptional -> nameOptional.map(name -> {
                synchronized (usernameLock) {
                    return username = name;
                }
            }).orElse(uuid.toString())).whenComplete((result, ex) -> {
                synchronized (usernameRequestLock) {
                    usernameRequest = null;
                }
            });
        }
    }

    @Override
    public @NotNull UUID getUUID() {
        return uuid;
    }

    @Override
    public @NotNull CompletableFuture<String> getUsername() {
        Player player = playerReference.get();
        if (player != null) {
            return CompletableFuture.completedFuture(username = player.getUsername());
        }

        synchronized (usernameLock) {
            if (username != null) {
                return CompletableFuture.completedFuture(username);
            }

            return getUsernameRequest();
        }
    }

    @Override
    public @NotNull CompletableFuture<Component> getDisplayName() {
        return getPlayer().map(Player::getDisplayName).map(CompletableFuture::completedFuture)
                .orElseGet(() -> getUsername().thenApply(Component::text));

    }

    @Override
    public @NotNull Optional<Player> getPlayer() {
        //first, try to get the cached player
        Player player = playerReference.get();
        if (player != null && player.isOnline()) {
            return Optional.of(player);
        }

        //if null or offline, update the reference (may still be null)
        player = connectionManager.getPlayer(uuid);

        if (player == null) {
            playerReference = NULL_REFERENCE;
            return Optional.empty();
        }
        else {
            playerReference = new WeakReference<>(player);
            return Optional.of(player);
        }
    }

}
