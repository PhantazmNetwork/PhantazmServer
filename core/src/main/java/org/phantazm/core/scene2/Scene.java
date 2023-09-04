package org.phantazm.core.scene2;

import net.minestom.server.Tickable;
import net.minestom.server.thread.Acquirable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.player.PlayerView;

import java.util.Set;

/**
 * Represents something that can be joined by players, such as a game or a lobby. Scenes are ticked by a
 * {@link SceneManager}.
 * <p>
 * <h2>Thread Safety</h2>
 * Unless otherwise stated, all methods on Scene are <i>not</i> thread safe; the scene must be acquired using the
 * {@link Acquirable} API before modifications are made.
 */
public interface Scene extends Tickable, Acquirable.Source<Scene> {
    /**
     * Gets all players currently in this scene.
     *
     * @return all players currently in the scene
     */
    @NotNull @Unmodifiable Set<PlayerView> players();

    /**
     * Returns whether this scene is joinable under any circumstances. When determining where to send player(s), this
     * can be queried to filter out the most obviously impossible candidates before performing more detailed checks.
     * <p>
     * This method must also return false if:
     * <ul>
     *     <li>The scene has been shut down by a previous call to {@link Scene#shutdown()} (i.e. when {@link Scene#isShutdown()} returns {@code true})</li>
     * </ul>
     *
     * @return true if this scene can be joined, false otherwise
     */
    boolean joinable();

    /**
     * Whether this scene should prevent an orderly server shutdown. This should be {@code true} for ongoing games with
     * players that have not finished yet, but {@code false} for finished games, empty games, or lobbies.
     *
     * @return true if this scene prevents shutdown, false otherwise
     */
    boolean preventsServerShutdown();

    /**
     * Checks if the scene has been previously shut down via a call to {@link Scene#shutdown()}.
     *
     * @return {@code true} if the scene has been shut down prior, {@code false} otherwise
     */
    boolean isShutdown();

    /**
     * Shuts down this scene. Broadly speaking, this will have the following effects:
     * <ul>
     *     <li>The scene will no longer accept new players; {@link Scene#joinable()} will return {@code false}.</li>
     *     <li>Subsequent calls to {@link Scene#isShutdown()} will return {@code true}.</li>
     *     <li>The scene will clean up any otherwise persistent resources it may be using, such as instances, event hooks, or scheduled tasks.</li>
     *     <li>The scene will NOT remove any players present in the scene; it is expected that a {@link SceneManager} will call {@link Scene#leave(Set)} for the remaining players</li>
     *     <li>As a consequence of the previous requirement, after calling this method, the set returned by {@link Scene#players()} should never change so long as {@link Scene#leave(Set)} is not called.</li>
     * </ul>
     * <p>
     * Shutting down a scene that has already shut down will do nothing.
     * <p>
     * This method is marked as internal because it should only be called indirectly through
     * {@link SceneManager#removeScene(Scene)}, in order to prevent shut-down scenes from sticking around in the
     * manager, ensure players are properly removed, and prevent synchronization issues.
     */
    @ApiStatus.Internal
    void shutdown();

    /**
     * Removes some number of players from this scene, updating internal state as needed to account for this.
     * <p>
     * This method <i>must</i>:
     * <ul>
     *     <li>Ignore offline players</li>
     *     <li>Ignore players that have never been added to the scene</li>
     *     <li>Ignore players that are in a different scene</li>
     *     <li>Modify the state of players that <i>are</i> present in this scene such as to remove any and all modifications applied by this scene</li>
     * </ul>
     * <p>
     * This method must <i>not</i>:
     * <ul>
     *     <li>Send the player(s) to a new scene</li>
     *     <li>Attempt to remove players from other scenes</li>
     *     <li>Kick the player(s)</li>
     * </ul>
     * <p>
     * This method is marked as internal because it should generally only be called by {@link SceneManager}; this method
     * is only used if it can be determined that a player will be accepted by a different scene, or if they are
     * disconnecting from the server (in which case there is no other scene).
     *
     * @param players the players who are leaving, some of which may not be online, or even present in the scene
     */
    @ApiStatus.Internal
    void leave(@NotNull Set<? extends @NotNull PlayerView> players);
}
