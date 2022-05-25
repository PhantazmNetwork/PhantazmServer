package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.api.VecUtils;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.agent.Descriptor;
import com.github.phantazmnetwork.neuron.agent.Explorer;
import com.github.phantazmnetwork.neuron.agent.TranslationExplorer;
import com.github.phantazmnetwork.neuron.bindings.minestom.ContextProvider;
import com.github.phantazmnetwork.neuron.engine.PathContext;
import com.github.phantazmnetwork.neuron.navigator.Controller;
import com.github.phantazmnetwork.neuron.navigator.Navigator;
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
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Root of all entities that use Neuron for pathfinding. This is both a {@link LivingEntity} and an {@link Agent}.
 */
public class NeuralEntity extends LivingEntity implements Agent {
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
                && !instance.isInVoid(getPosition()) && currentChunk.isLoaded() && vehicle == null && descriptor
                .canPathfind(this);
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
        Navigator navigator = getNavigator();
        if(target == null) {
            navigator.setDestination(null);
        }
        else {
            navigator.setDestination(() -> descriptor.computeTargetPosition(target));
        }
    }

    @Override
    public CompletableFuture<Void> setInstance(@NotNull Instance instance, @NotNull Pos spawnPosition) {
        return super.setInstance(instance, spawnPosition).thenRun(() -> {
            PathContext context = provider.provideContext(instance);

            cancelNavigation();
            navigator = descriptor.makeNavigator(context, this);
            controller = descriptor.makeController(this);

            explorer = new TranslationExplorer(null, descriptor, descriptor.makeTranslator(instance, context));
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
