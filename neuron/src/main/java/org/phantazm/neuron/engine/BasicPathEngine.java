package org.phantazm.neuron.engine;

import com.github.steanky.vector.Vec3I;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.MathUtils;
import org.phantazm.neuron.agent.Agent;
import org.phantazm.neuron.agent.Descriptor;
import org.phantazm.neuron.navigator.Controller;
import org.phantazm.neuron.operation.BasicPathOperation;
import org.phantazm.neuron.operation.PathOperation;
import org.phantazm.neuron.operation.PathResult;

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
