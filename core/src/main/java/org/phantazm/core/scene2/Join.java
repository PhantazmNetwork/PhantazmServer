package org.phantazm.core.scene2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.player.PlayerView;

import java.util.Set;

/**
 * Represents a request by some number of players to join a specific scene. Join instances are expected to be passed to
 * a {@link SceneManager} that will fulfill the request.
 * <h2>Thread Safety</h2>
 * Unless otherwise specified, no methods on Join support concurrent access.
 *
 * @param <T> the type of scene to join
 */
public interface Join<T extends Scene> {
    /**
     * Retrieves the set of all players that are participating in this Join. The contents of this set, as well as the
     * set instance itself, should never change over the lifetime of this object.
     *
     * @return the set of all joining players
     */
    @NotNull @Unmodifiable Set<@NotNull PlayerView> players();

    /**
     * The type of {@link Scene} this Join wants. {@link SceneManager} will use this value to determine what types of
     * scene may be used. This is an <i>exact match</i>; subclasses of {@code T} will not be considered for joining,
     * even if such a cast would be technically safe.
     * <p>
     * The value returned by this method should not change over the lifetime of this object.
     *
     * @return the kind of scene we must join
     */
    @NotNull Class<T> targetType();

    /**
     * Creates a new Scene which can be used to fulfill this request. If {@link Join#canCreateNewScene()} returns
     * {@code false}, this method will throw an exception. Otherwise, the scene returned by this method <i>must</i> have
     * the following characteristics:
     *
     * <ul>
     *     <li>The scene must not already be a part of a {@link SceneManager}.</li>
     *     <li>The scene must match this Join; that is, {@link Join#matches(Scene)} will return {@code true} if passed the new scene instance.</li>
     *     <li>As a consequence of the previous requirement, calling {@link Join#join(Scene)} on the scene must succeed without throwing an exception.</li>
     * </ul>
     *
     * @return a new Scene
     */
    @NotNull T createNewScene();

    /**
     * Whether this Join may result in the creation of a new Scene. In general, {@link SceneManager} will prefer to use
     * an existing scene to fulfill this request, even if this method returns true.
     *
     * @return {@code true} if this join may result in a new Scene
     */
    boolean canCreateNewScene();

    /**
     * Joins the given scene. {@code scene} <i>must</i> be acquired when this method is called, so it is safe to use any
     * method on the object. This method <i>may</i> throw an unchecked exception when called for any scene {@code scene}
     * such that {@code Join#matches(scene)} returns {@code false}, unless {@link Join#canCreateNewScene()} returns
     * {@code true} and the scene was freshly created using {@link Join#createNewScene()}, in which case the join will
     * always succeed.
     * <p>
     * This method is expected to (either directly or indirectly) perform actions such as teleporting players to a new
     * instance, sending tablist packets, and other modifying operations, as appropriate. This method should <i>not</i>
     * modify players that are not present in {@link Join#players()}.
     *
     * @param scene the scene to join
     */
    void join(@NotNull T scene);

    /**
     * Determines if the existing scene {@code scene} can be used to fulfill this Join. If this method returns true,
     * {@link Join#join(Scene)} will succeed. {@link SceneManager} always queries this method before attempting to
     * fulfill a join.
     *
     * @param scene the scene to join
     * @return true if the scene may fulfill this Join, false otherwise
     */
    boolean matches(@NotNull T scene);
}
