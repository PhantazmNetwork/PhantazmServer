package com.github.phantazmnetwork.neuron.engine;

import com.github.phantazmnetwork.commons.MathUtils;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.agent.Descriptor;
import com.github.phantazmnetwork.neuron.navigator.Controller;
import com.github.phantazmnetwork.neuron.operation.BasicPathOperation;
import com.github.phantazmnetwork.neuron.operation.PathOperation;
import com.github.phantazmnetwork.neuron.operation.PathResult;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

/**
 * A simple, asynchronous {@link PathEngine} implementation. {@link PathOperation}s are completed using the provided
 * {@link ExecutorService}.
 */
public class BasicPathEngine implements PathEngine {
    private final ExecutorService executor;

    /**
     * Creates a new BasicPathEngine from the given ExecutorService. The lifecycle of the executor is not managed by
     * this class; thus, care must be taken to properly terminate the service if it is no longer needed.
     *
     * @param executor the {@link ExecutorService} used to run pathfinding operations asynchronously
     */
    public BasicPathEngine(@NotNull ExecutorService executor) {
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    @Override
    public @NotNull Future<PathResult> pathfind(@NotNull Agent agent, @NotNull Vec3I destination) {
        try {
            return executor.submit(() -> {
                if (!agent.canPathfind()) {
                    return null;
                }

                Controller controller = agent.getController();
                Descriptor descriptor = agent.getDescriptor();

                PathOperation operation = new BasicPathOperation(agent.getStartPosition(), destination,
                        pos -> descriptor.isComplete(pos, destination), descriptor.getCalculator(), agent.getExplorer(),
                        (float)MathUtils.floorOffset(controller.getX()),
                        (float)MathUtils.floorOffset(controller.getZ()));

                while (!operation.isComplete() && !Thread.interrupted()) {
                    //step the operation until complete or we're interrupted
                    operation.step();
                }

                return operation.isComplete() ? operation.getResult() : null;
            });
        }
        catch (RejectedExecutionException ignored) {
            return CompletableFuture.completedFuture(null);
        }
    }
}
