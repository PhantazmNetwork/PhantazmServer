package com.github.phantazmnetwork.neuron.navigator;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Represents a class responsible for handling {@link Agent} navigation.
 */
public interface Navigator extends Tickable {
    /**
     * Sets the current destination. If null, navigation will be cancelled. The supplier will be regularly queried in
     * order to ensure navigation is up-to-date.
     * @param destination the destination supplier, which may be null to cancel navigation
     */
    void setDestination(@Nullable Supplier<Vec3I> destination);

    /**
     * Gets the {@link Agent} this navigator manages.
     * @return the agent managed by this navigator
     */
    @NotNull Agent getAgent();

    /**
     * Can be used to determine if this navigator has an active destination (and thus is navigating towards it).
     * @return {@code true} if this navigator has an active destination, false otherwise
     */
    boolean hasDestination();

    /**
     * Gets the current destination. If this navigator does not have a destination (if
     * {@link Navigator#hasDestination()} returns {@code false}), this method will throw an
     * {@link IllegalStateException}.
     * @return the current destination
     * @throws IllegalStateException if this navigator does not have a destination
     */
    @NotNull Vec3I getDestination();
}
