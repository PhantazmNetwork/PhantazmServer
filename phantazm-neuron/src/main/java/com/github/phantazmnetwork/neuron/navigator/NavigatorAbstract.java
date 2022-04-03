package com.github.phantazmnetwork.neuron.navigator;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.engine.PathEngine;
import com.github.phantazmnetwork.neuron.operation.PathResult;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.Future;

public abstract class NavigatorAbstract implements Navigator {
    private final PathEngine pathEngine;
    private final Agent agent;

    private Future<PathResult> currentOperation;
    private Vec3I currentDestination;

    public NavigatorAbstract(@NotNull PathEngine pathEngine, @NotNull Agent agent) {
        this.pathEngine = Objects.requireNonNull(pathEngine, "pathEngine");
        this.agent = Objects.requireNonNull(agent, "agent");
    }

    @Override
    public void tick(long time) {

    }

    @Override
    public void setDestination(@NotNull Vec3I destination) {
        if(!destination.equals(currentDestination)) {
            currentDestination = destination;
            if(currentOperation != null) {
                currentOperation.cancel(true);
            }

            currentOperation = pathEngine.pathfind(agent, destination);
        }
    }
}
