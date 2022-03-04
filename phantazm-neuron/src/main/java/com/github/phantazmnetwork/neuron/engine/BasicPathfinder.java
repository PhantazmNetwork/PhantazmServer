package com.github.phantazmnetwork.neuron.engine;

import com.github.phantazmnetwork.commons.vector.ImmutableVec3I;
import com.github.phantazmnetwork.commons.vector.Vec3I;
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
        private final Vec3I start;
        private final Vec3I destination;

        private Context(Agent agent, Vec3I start, int toX, int toY, int toZ) {
            this.agent = agent;
            this.start = start;
            this.destination = new ImmutableVec3I(toX, toY, toZ);
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
        public @NotNull Vec3I getStartPosition() {
            return start;
        }

        @Override
        public @NotNull Vec3I getDestination() {
            return destination;
        }
    }

    private final Function<PathContext, PathOperation> operationFunction;

    public BasicPathfinder(@NotNull Function<PathContext, PathOperation> operationFunction) {
        this.operationFunction = Objects.requireNonNull(operationFunction, "operationFunction");
    }

    @Override
    public @NotNull Future<PathResult> pathfind(@NotNull Agent agent, int destX, int destY, int destZ) {
        PathContext context = new Context(Objects.requireNonNull(agent, "agent"), Objects.requireNonNull(agent
                .computeStartPosition(), "computed start position"), destX, destY, destZ);
        PathOperation operation = operationFunction.apply(context);

        while(!operation.isComplete()) {
            operation.step();
        }

        return CompletableFuture.completedFuture(operation.getResult());
    }
}
