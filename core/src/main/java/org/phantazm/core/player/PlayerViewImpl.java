package org.phantazm.core.player;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.ReferenceUtils;
import org.phantazm.core.scene2.Scene;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.phantazm.core.scene2.SceneManager;

/**
 * <p>Basic implementation of a {@link PlayerView}. Caches the player object, so {@code getPlayer} is safe to call
 * frequently. Additionally, these objects are safe for long-term storage because they do not store a strong reference
 * to the player.</p>
 *
 * <p>This class is not part of the public API. Instances may be obtained through a suitable {@link PlayerViewProvider}
 * implementation.</p>
 *
 * @see Player
 * @see BasicPlayerViewProvider
 */
@ApiStatus.Internal
public final class PlayerViewImpl implements PlayerView {
    private final IdentitySource identitySource;
    private final ConnectionManager connectionManager;
    private final UUID uuid;

    private final Lock sceneJoinLock = new ReentrantLock();
    private final Object usernameLock = new Object();
    private final Object usernameRequestLock = new Object();

    private final TagHandler tags;

    private final int hashCode;

    private volatile Reference<Scene> currentSceneReference;
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
    PlayerViewImpl(@NotNull IdentitySource identitySource, @NotNull ConnectionManager connectionManager,
        @NotNull UUID uuid) {
        this.identitySource = Objects.requireNonNull(identitySource);
        this.connectionManager = Objects.requireNonNull(connectionManager);
        this.uuid = Objects.requireNonNull(uuid);
        this.playerReference = ReferenceUtils.nullReference();
        this.currentSceneReference = ReferenceUtils.nullReference();

        this.hashCode = uuid.hashCode();
        this.tags = TagHandler.newHandler();
    }

    /**
     * Creates a basic {@link PlayerView} given a {@link ConnectionManager} and an already-existing {@link Player}.
     *
     * @param identitySource    The {@link IdentitySource} instance used to resolve usernames when necessary
     * @param connectionManager the ConnectionManager used to find {@link Player}s based on their {@link UUID}
     * @param player            the player, whose UUID and username will be cached immediately
     */
    PlayerViewImpl(@NotNull IdentitySource identitySource, @NotNull ConnectionManager connectionManager,
        @NotNull Player player) {
        this.identitySource = Objects.requireNonNull(identitySource);
        this.connectionManager = Objects.requireNonNull(connectionManager);
        this.uuid = player.getUuid();
        this.playerReference = new WeakReference<>(player);
        this.username = player.getUsername();
        this.currentSceneReference = ReferenceUtils.nullReference();

        this.hashCode = uuid.hashCode();
        this.tags = TagHandler.newHandler();
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
    public @NotNull Optional<String> getUsernameIfCached() {
        return Optional.ofNullable(username);
    }

    @Override
    public @NotNull CompletableFuture<Component> getDisplayName() {
        return getPlayer().map(Player::getDisplayName).map(CompletableFuture::completedFuture)
            .orElseGet(() -> getUsername().thenApply(Component::text));

    }

    @Override
    public @NotNull Optional<Component> getDisplayNameIfCached() {
        return getPlayer().map(Player::getDisplayName).or(() -> getUsernameIfCached().map(Component::text));
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
            playerReference = ReferenceUtils.nullReference();
            return Optional.empty();
        } else {
            playerReference = new WeakReference<>(player);
            return Optional.of(player);
        }
    }

    @Override
    public @NotNull TagHandler tagHandler() {
        return tags;
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
        return "LinkedPlayerView[uuid=" + uuid + ", username=" + username + ", linkedPlayer=" + playerReference.get() + "]";
    }

    /**
     * Gets the player reference, if it is set. Marked as internal because it is only useful in niche circumstances for
     * performance. For example, it may prevent an unnecessary lookup to {@link Entity#getEntity(UUID)}. It is only
     * meant to be used within {@link SceneManager}.
     * <p>
     * Note that a null reference does not <i>necessarily</i> mean that the player is offline. Likewise, a non-null
     * reference does not mean the player isn't offline.
     *
     * @return the player reference, which will be null if unset
     */
    @ApiStatus.Internal
    public @Nullable Player reference() {
        return playerReference.get();
    }

    /**
     * Returns the {@link Lock} used to synchronize scene join attempts by this player.
     * <p>
     * This method is marked internal because it is only useful when called by {@link SceneManager}, and it is easy for
     * users to corrupt internal state (for example by erroneously locking the player, which would prevent them from
     * ever being able to join a scene).
     *
     * @return the semaphore used to synchronize join attempts
     */
    @ApiStatus.Internal
    public @NotNull Lock joinLock() {
        return sceneJoinLock;
    }

    /**
     * Gets the player's current scene. The optional may be empty if:
     * <ul>
     *     <li>The player is not part of a scene; such as if they are offline</li>
     *     <li>The player previously had a scene but the scene was garbage collected</li>
     *     <li>The player is in a transitory period between having left a scene and being added to a new one</li>
     * </ul>
     * <p>
     * This method is marked internal because it should only be called by {@link SceneManager}, as it will perform the
     * correct synchronization. To retrieve the player's current scene, please use
     * {@link SceneManager#currentScene(PlayerView)}.
     *
     * @return an Optional containing the current scene, or {@code null} if there is none
     */
    @ApiStatus.Internal
    public @NotNull Optional<Scene> currentScene() {
        return Optional.ofNullable(currentSceneReference.get());
    }

    /**
     * Updates the player's current scene. This should only be called by a thread that's able to acquire the monitor
     * from {@link PlayerViewImpl#joinLock()}.
     * <p>
     * This method is marked internal because its access must be carefully synchronized by {@link SceneManager}.
     *
     * @param scene the scene to update to; {@code null} to set no scene
     */
    @ApiStatus.Internal
    public void updateCurrentScene(@Nullable Scene scene) {
        this.currentSceneReference = scene == null ? ReferenceUtils.nullReference() : new WeakReference<>(scene);
    }
}
