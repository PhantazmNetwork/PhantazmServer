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

import javax.naming.ldap.Control;
import java.util.Objects;
import java.util.concurrent.*;

@SuppressWarnings("ClassCanBeRecord")
public class BasicPathEngine implements PathEngine {
    private final ExecutorService executor;

    public BasicPathEngine(@NotNull ExecutorService executor) {
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    @Override
    public @NotNull Future<PathResult> pathfind(@NotNull Agent agent, @NotNull Vec3I destination) {
        try {
            return executor.submit(() -> {
                if(!agent.hasStartPosition()) {
                    return null;
                }

                Controller controller = agent.getController();
                Descriptor descriptor = agent.getDescriptor();

                PathOperation operation = new BasicPathOperation(agent.getStartPosition(), destination, pos ->
                        descriptor.isComplete(pos, destination), descriptor.getCalculator(), agent.getExplorer(),
                        (float) MathUtils.floorOffset(controller.getX()), (float) MathUtils.floorOffset(controller
                        .getZ()));

                while(!operation.isComplete() && !Thread.interrupted()) {
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
