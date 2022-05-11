package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.api.vector.VecUtils;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.agent.Descriptor;
import com.github.phantazmnetwork.neuron.agent.Explorer;
import com.github.phantazmnetwork.neuron.agent.TranslationExplorer;
import com.github.phantazmnetwork.neuron.bindings.minestom.ContextProvider;
import com.github.phantazmnetwork.neuron.engine.PathContext;
import com.github.phantazmnetwork.neuron.navigator.Controller;
import com.github.phantazmnetwork.neuron.navigator.Navigator;
import com.github.phantazmnetwork.neuron.node.NodeTranslator;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Root of all entities that use Neuron for pathfinding. This is both a {@link LivingEntity} and an {@link Agent}.
 */
public abstract class NeuralEntity extends LivingEntity implements Agent {
    private final MinestomDescriptor descriptor;
    private final ContextProvider provider;

    private Navigator navigator;
    private Controller controller;
    private Explorer explorer;

    private int removalAnimationDelay;

    /**
     * Creates a new NeuralEntity.
     * @param descriptor the descriptor used to define this agent's characteristics
     * @param uuid this entity's unique ID
     * @param provider the {@link ContextProvider} used to provide {@link PathContext} instances for this entity
     */
    public NeuralEntity(@NotNull MinestomDescriptor descriptor, @NotNull UUID uuid, @NotNull ContextProvider provider) {
        super(descriptor.getEntityType(), uuid);
        this.descriptor = descriptor;
        this.provider = Objects.requireNonNull(provider, "provider");
        this.removalAnimationDelay = 1000;
    }

    @Override
    public void preTick(long time) {
        navigator.tick(time);
    }

    @Override
    public boolean canPathfind() {
        Instance instance = this.instance;
        Chunk currentChunk = this.currentChunk;
        return instance != null && currentChunk != null && !isDead() && instance.getWorldBorder().isInside(this)
                && !instance.isInVoid(getPosition()) && currentChunk.isLoaded() && vehicle == null;
    }

    @Override
    public @NotNull Vec3I getStartPosition() {
        return VecUtils.toBlockInt(getPosition());
    }

    @Override
    public @NotNull Descriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public @NotNull Explorer getExplorer() {
        requireInstance();
        return explorer;
    }

    @Override
    public @NotNull Controller getController() {
        requireInstance();
        return controller;
    }

    /**
     * Returns the {@link Navigator} managing this entity. Will throw an exception if this entity does not have an
     * instance defined yet.
     * @return the Navigator used for pathfinding
     */
    public @NotNull Navigator getNavigator() {
        requireInstance();
        return navigator;
    }

    /**
     * Sets the given entity as a navigation target for this entity. If null, pathfinding will be cancelled. If
     * non-null, this entity will follow the given entity according to its navigation capabilities.
     * @param target the target entity to follow
     */
    public void setTarget(@Nullable Entity target) {
        if(target == null) {
            navigator.setDestination(null);
        }
        else {
            navigator.setDestination(() -> VecUtils.toBlockInt(target.getPosition()));
        }
    }

    @Override
    public CompletableFuture<Void> setInstance(@NotNull Instance instance, @NotNull Pos spawnPosition) {
        return super.setInstance(instance, spawnPosition).whenComplete((ignored, ex) -> {
            PathContext context = provider.provideContext(instance);

            cancelNavigation();
            navigator = makeNavigator(context);
            controller = makeController();

            explorer = new TranslationExplorer(context.getCache(), descriptor.getID(), makeTranslator(instance,
                    context), getStepDirections());
        });
    }

    @Override
    public void kill() {
        super.kill();

        if (removalAnimationDelay > 0) {
            // Needed for proper death animation (wait for it to finish before destroying the entity)
            scheduleRemove(Duration.of(removalAnimationDelay, TimeUnit.MILLISECOND));
        } else {
            // Instant removal without animation playback
            remove();
        }
    }

    @Override
    public void remove() {
        super.remove();
        cancelNavigation();
    }

    /**
     * Gets the kill animation delay before vanishing the entity.
     *
     * @return the removal animation delay in milliseconds, 0 if not any
     */
    public int getRemovalAnimationDelay() {
        return removalAnimationDelay;
    }

    /**
     * Changes the removal animation delay of the entity.
     * <p>
     * Testing shows that 1000 is the minimum value to display the death particles.
     *
     * @param removalAnimationDelay the new removal animation delay in milliseconds, 0 to remove it
     */
    public void setRemovalAnimationDelay(int removalAnimationDelay) {
        this.removalAnimationDelay = removalAnimationDelay;
    }

    /**
     * Calls an {@link EntityAttackEvent} with this entity as the source and {@code target} as the target.
     *
     * @param target    the entity target
     * @param swingHand true to swing the entity main hand, false otherwise
     */
    public void attack(@NotNull Entity target, boolean swingHand) {
        if(swingHand) {
            swingMainHand();
        }

        EventDispatcher.call(new EntityAttackEvent(this, target));
    }

    /**
     * Calls a {@link EntityAttackEvent} with this entity as the source and {@code target} as the target.
     * <p>
     * This does not trigger the hand animation.
     *
     * @param target the entity target
     */
    public void attack(@NotNull Entity target) {
        attack(target, false);
    }

    /**
     * Creates a {@link Navigator} instance given a {@link PathContext}.
     * @param context the current PathContext
     * @return a new Navigator instance to use for navigation
     */
    protected abstract @NotNull Navigator makeNavigator(@NotNull PathContext context);

    /**
     * Creates a {@link NodeTranslator} instance given an {@link Instance} and {@link PathContext}.
     * @param instance the current instance
     * @param context the current PathContext
     * @return a new NodeTranslator instance to use for navigation
     */
    protected abstract @NotNull NodeTranslator makeTranslator(@NotNull Instance instance, @NotNull PathContext context);

    /**
     * Creates a {@link Controller} suitable for making this entity move along a path.
     * @return a Controller suitable for this entity's movement
     */
    protected abstract @NotNull Controller makeController();

    /**
     * Returns an Iterable over the directions this entity may step during navigation.
     * @return an Iterable over the directions this entity may step during navigation
     */
    protected abstract @NotNull Iterable<Vec3I> getStepDirections();

    private void cancelNavigation() {
        if(navigator != null) {
            navigator.setDestination(null);
        }
    }

    private void requireInstance() {
        if(instance == null) {
            throw new IllegalStateException("Entity has no instance set");
        }
    }
}
