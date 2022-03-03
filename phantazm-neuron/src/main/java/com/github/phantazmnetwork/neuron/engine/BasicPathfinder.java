package com.github.phantazmnetwork.neuron.engine;

import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.operation.BasicPathOperation;
import com.github.phantazmnetwork.neuron.operation.PathContext;
import com.github.phantazmnetwork.neuron.operation.PathOperation;
import com.github.phantazmnetwork.neuron.operation.PathResult;
import com.github.phantazmnetwork.neuron.vector.Vec3I;
import com.github.phantazmnetwork.neuron.world.Space;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;

public class BasicPathfinder implements Pathfinder {
    private class Context implements PathContext {
        private final Space space;
        private final Agent agent;
        private final Destination destination;

        private Context(@NotNull Space space, @NotNull Agent agent, @NotNull Destination destination) {
            this.space = space;
            this.agent = agent;
            this.destination = destination;
        }

        @Override
        public @NotNull Space getSpace() {
            return space;
        }

        @Override
        public @NotNull Agent getAgent() {
            return agent;
        }

        @Override
        public @NotNull Pathfinder getPathfinder() {
            return BasicPathfinder.this;
        }

        @Override
        public @NotNull Destination getDestination() {
            return destination;
        }
    }

    private final Space space;
    private final Function<PathContext, PathOperation> operationFunction;

    public BasicPathfinder(@NotNull Space space, @NotNull Function<PathContext, PathOperation> operationFunction) {
        this.space = Objects.requireNonNull(space, "space");
        this.operationFunction = Objects.requireNonNull(operationFunction, "operationFunction");
    }

    @Override
    public @NotNull Future<PathResult> pathfind(@NotNull Agent agent, @NotNull Destination destination) {
        PathContext context = new Context(space, agent, destination);
        PathOperation operation = operationFunction.apply(context);
        while(!operation.isComplete()) {
            operation.step();
        }

        return CompletableFuture.completedFuture(operation.getResult());
    }
}
