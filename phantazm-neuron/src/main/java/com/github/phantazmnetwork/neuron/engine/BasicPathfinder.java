package com.github.phantazmnetwork.neuron.engine;

import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.operation.PathContext;
import com.github.phantazmnetwork.neuron.operation.PathOperation;
import com.github.phantazmnetwork.neuron.operation.PathResult;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;

public class BasicPathfinder implements Pathfinder {
    private class Context implements PathContext {
        private final Agent agent;
        private final Destination destination;

        private Context(@NotNull Agent agent, @NotNull Destination destination) {
            this.agent = agent;
            this.destination = destination;
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

    private final Function<PathContext, PathOperation> operationFunction;

    public BasicPathfinder(@NotNull Function<PathContext, PathOperation> operationFunction) {
        this.operationFunction = Objects.requireNonNull(operationFunction, "operationFunction");
    }

    @Override
    public @NotNull Future<PathResult> pathfind(@NotNull Agent agent, @NotNull Destination destination) {
        PathContext context = new Context(agent, destination);
        PathOperation operation = operationFunction.apply(context);
        while(!operation.isComplete()) {
            operation.step();
        }

        return CompletableFuture.completedFuture(operation.getResult());
    }
}
