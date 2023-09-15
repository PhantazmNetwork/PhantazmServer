package org.phantazm.core.scene2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.player.PlayerView;

import java.util.Set;

/**
 * Represents a request by some number of players to join a specific scene. Join instances are expected to be passed to
 * a {@link SceneManager} that will fulfill the request. Consequently, Join instances are most commonly used as if by a
 * visitor pattern.
 * <p>
 * Manually creating Join instances whenever it is necessary to move players around may be inconvenient. Therefore,
 * users of this API should consider registering {@link SceneManager.JoinFunction} instances to the SceneManager, which
 * are associated with a {@link SceneManager.Key}; then, sending players between scenes is as simple as calling
 * {@link SceneManager#joinScene(SceneManager.Key, Set)} passing in the key and the set of players that should join.
 * <p>
 * <h2>Thread Safety</h2>
 * Unless otherwise specified, no methods on Join support concurrent access.
 *
 * @param <T> the type of scene to join
 * @see SceneManager.JoinFunction
 * @see SceneManager#registerJoinFunction(SceneManager.Key, SceneManager.JoinFunction)
 */
public interface Join<T extends Scene> {
    /**
     * Retrieves the set of all players that are participating in this Join. The contents of this set, as well as the
     * set instance itself, should never change over the lifetime of this object. If empty, the {@link SceneManager}
     * will ignore this Join.
     * <p>
     * Otherwise, there are almost no restrictions on the players that can be present in this set. They may be offline,
     * spread across various scenes, or have no current scene.
     *
     * @return the set of all joining players
     */
    @NotNull @Unmodifiable Set<@NotNull PlayerView> playerViews();

    /**
     * The type of {@link Scene} this Join wants. {@link SceneManager} will use this value to determine what types of
     * scene may be used. This is an <i>exact match</i>; subclasses of {@code T} will not be considered for joining,
     * even if such a cast would be technically safe.
     * <p>
     * The value returned by this method should not change over the lifetime of this object. Consequently, it should be
     * threadsafe.
     *
     * @return the kind of scene we must join
     */
    @NotNull Class<T> targetType();

    /**
     * Creates a new Scene which can be used to fulfill this request. If {@link Join#canCreateNewScene(SceneManager)}
     * returns {@code false}, this method <i>may</i> throw an exception. Otherwise, so long as {@code canCreateNewScene}
     * returns {@code true}, the scene returned by this method <i>must</i> have the following characteristics:
     *
     * <ul>
     *     <li>The scene must not already be a part of a {@link SceneManager}.</li>
     *     <li>The scene must match this Join; that is, {@link Join#matches(Scene)} will return {@code true} if passed the new scene instance.</li>
     *     <li>As a consequence of the previous requirement, calling {@link Join#join(Scene)} on the scene must succeed without throwing an exception.</li>
     * </ul>
     * <p>
     * It is expected that the returned scene will be added to the SceneManager fulfilling this join by the manager
     * itself, which will make it eligible to fulfill future joins.
     *
     * @param manager the SceneManager responsible for fulfilling this Join
     * @return a new Scene
     */
    @NotNull T createNewScene(@NotNull SceneManager manager);

    /**
     * Whether this Join may result in the creation of a new Scene. In general, {@link SceneManager} will prefer to use
     * an existing scene to fulfill this request, even if this method returns true.
     *
     * @param manager the {@link SceneManager} that is processing this join
     * @return {@code true} if this join may result in a new Scene
     */
    boolean canCreateNewScene(@NotNull SceneManager manager);

    /**
     * Joins the given scene. {@code scene} <i>must</i> be acquired when this method is called, so it is safe to use any
     * method on the object. This method <i>may</i> throw an unchecked exception when called for any scene {@code scene}
     * such that {@code Join#matches(scene)} returns {@code false}, unless {@link Join#canCreateNewScene(SceneManager)}
     * returns {@code true} and the scene was freshly created using {@link Join#createNewScene(SceneManager)}, in which
     * case the join must always succeed as per the specification of scene creation.
     * <p>
     * This method is expected to (either directly or indirectly) perform actions such as teleporting players to a new
     * instance, sending tablist packets, and other modifying operations, as appropriate. This method should <i>not</i>
     * modify players that are not present in {@link Join#playerViews()}.
     * <p>
     * When sending players to scenes, it is important to note that (by default) there is no mechanism preventing a
     * player from participating in a join for a scene which they are already a part of. Join implementations must be
     * aware of this fact, and may wish to take such players into account when determining if a scene is eligible.
     *
     * @param scene the scene to join
     */
    void join(@NotNull T scene);

    /**
     * Determines if the existing scene {@code scene} can be used to fulfill this Join. If this method returns true
     * while {@code scene} is acquired, {@link Join#join(Scene)} must succeed so long as the scene is still acquired
     * since the previous call to {@code matches}. {@link SceneManager} always queries this method before attempting to
     * fulfill a join.
     *
     * @param scene the scene to join
     * @return true if the scene may fulfill this Join, false otherwise
     */
    boolean matches(@NotNull T scene);
}
