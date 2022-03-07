package com.github.phantazmnetwork.neuron.engine;

import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.node.Destination;
import com.github.phantazmnetwork.neuron.operation.BasicPathOperation;
import com.github.phantazmnetwork.neuron.operation.PathContext;
import com.github.phantazmnetwork.neuron.operation.PathOperation;
import com.github.phantazmnetwork.neuron.operation.PathResult;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * A simple, synchronous {@link Pathfinder} implementation.
 */
public class BasicPathfinder implements Pathfinder {
    private class Context implements PathContext {
        private final Agent agent;
        private final Destination destination;

        private Context(Agent agent, Destination destination) {
            this.agent = agent;
            this.destination = destination;
        }

        @Override
        public @NotNull Pathfinder getPathfinder() {
            return BasicPathfinder.this;
        }

        @Override
        public @NotNull Agent getAgent() {
            return agent;
        }

        @Override
        public @NotNull Destination getDestination() {
            return destination;
        }
    }

    private final Function<PathContext, PathOperation> operationFunction;

    /**
     * Creates a BasicPathfinder instance that uses the provided {@link Function} to create {@link PathOperation}
     * instances for pathfinding.
     * @param operationFunction the function used to create PathOperation instances
     */
    public BasicPathfinder(@NotNull Function<PathContext, PathOperation> operationFunction) {
        this.operationFunction = Objects.requireNonNull(operationFunction, "operationFunction");
    }

    /**
     * Creates a BasicPathfinder instance that will use {@link BasicPathOperation} instances for pathfinding.
     */
    public BasicPathfinder() {
        this.operationFunction = BasicPathOperation::new;
    }

    @Override
    public @NotNull Future<PathResult> pathfind(@NotNull Agent agent, @NotNull Destination destination) {
        Objects.requireNonNull(agent, "agent");
        Objects.requireNonNull(destination, "destination");

        return CompletableFuture.completedFuture(operationFunction.apply(new Context(agent, destination))
                .runToCompletion());
    }
}
