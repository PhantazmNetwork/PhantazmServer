package com.github.phantazmnetwork.neuron.engine;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.agent.Descriptor;
import com.github.phantazmnetwork.neuron.operation.BasicPathOperation;
import com.github.phantazmnetwork.neuron.operation.PathResult;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.*;

@SuppressWarnings("ClassCanBeRecord")
public class BasicPathEngine implements PathEngine {
    private final ExecutorService service;

    public BasicPathEngine(@NotNull ExecutorService service) {
        this.service = Objects.requireNonNull(service, "service");
    }

    @Override
    public @NotNull Future<PathResult> pathfind(@NotNull Agent agent, @NotNull Vec3I destination) {
        return service.submit(() -> {
            if(!agent.hasStartPosition()) {
                return null;
            }

            Descriptor descriptor = agent.getDescriptor();
            return new BasicPathOperation(agent.getStartPosition(), destination, descriptor::isComplete,
                    descriptor.getCalculator(), agent.getExplorer()).run();
        });
    }

    @Override
    public void shutdown() {
        service.shutdown();

        try {
            if(!service.awaitTermination(10, TimeUnit.SECONDS)) {
                service.shutdownNow();
            }
        } catch (InterruptedException e) {
            service.shutdownNow();
        }
    }

    @Override
    public boolean isShutdown() {
        return service.isShutdown();
    }
}
