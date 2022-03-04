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
        private final Vec3I end;

        private Context(Agent agent, int startX, int startY, int startZ, int endX, int endY, int endZ) {
            this.agent = agent;
            this.start = new ImmutableVec3I(startX, startY, startZ);
            this.end = new ImmutableVec3I(endX, endY, endZ);
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
        public @NotNull Vec3I getStart() {
            return start;
        }

        @Override
        public @NotNull Vec3I getEnd() {
            return end;
        }
    }

    private final Function<PathContext, PathOperation> operationFunction;

    public BasicPathfinder(@NotNull Function<PathContext, PathOperation> operationFunction) {
        this.operationFunction = Objects.requireNonNull(operationFunction, "operationFunction");
    }

    @Override
    public @NotNull Future<PathResult> pathfind(@NotNull Agent agent, int startX, int startY, int startZ, int endX,
                                                int endY, int endZ) {
        PathContext context = new Context(Objects.requireNonNull(agent, "agent"), startX, startY, startZ, endX,
                endY, endZ);
        PathOperation operation = operationFunction.apply(context);

        while(!operation.isComplete()) {
            operation.step();
        }

        return CompletableFuture.completedFuture(operation.getResult());
    }
}
