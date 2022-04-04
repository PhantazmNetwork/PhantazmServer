package com.github.phantazmnetwork.neuron.engine;

import com.github.phantazmnetwork.commons.Wrapper;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.agent.Descriptor;
import com.github.phantazmnetwork.neuron.operation.BasicPathOperation;
import com.github.phantazmnetwork.neuron.operation.PathOperation;
import com.github.phantazmnetwork.neuron.operation.PathResult;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.*;

@SuppressWarnings("ClassCanBeRecord")
public class BasicPathEngine implements PathEngine {
    private final Executor executor;

    private class ResultFuture implements Future<PathResult> {
        private final Object cancelSync = new Object();

        private volatile boolean isDone;
        private volatile boolean isCancelled;
        private volatile PathResult result;

        private final CountDownLatch waitLatch = new CountDownLatch(1);

        private ResultFuture(Agent agent, Vec3I destination) {
            executor.execute(() -> {
                if(!agent.hasStartPosition()) {
                    synchronized (cancelSync) {
                        isDone = true;
                        waitLatch.countDown();
                        return;
                    }
                }

                Descriptor descriptor = agent.getDescriptor();
                PathOperation operation = new BasicPathOperation(agent.getStartPosition(), destination, (pos) ->
                        descriptor.isComplete(pos, destination), descriptor.getCalculator(), agent.getExplorer());

                while(!operation.isComplete() && !isCancelled) {
                    operation.step();
                }

                synchronized (cancelSync) {
                    isDone = true;
                    result = isCancelled ? null : operation.getResult();
                    waitLatch.countDown();
                }
            });
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            synchronized (cancelSync) {
                if(!isDone) {
                    isCancelled = true;
                    return true;
                }

                return false;
            }
        }

        @Override
        public boolean isCancelled() {
            return isCancelled;
        }

        @Override
        public boolean isDone() {
            synchronized (cancelSync) {
                return isDone || isCancelled;
            }
        }

        @Override
        public PathResult get() throws InterruptedException {
            waitLatch.await();
            synchronized (cancelSync) {
                if(isCancelled) {
                    throw new CancellationException();
                }

                return result;
            }
        }

        @Override
        public PathResult get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, TimeoutException {
            if(!waitLatch.await(timeout, unit)) {
                throw new TimeoutException();
            }

            synchronized (cancelSync) {
                if(isCancelled) {
                    throw new CancellationException();
                }

                return result;
            }
        }
    }

    public BasicPathEngine(@NotNull Executor executor) {
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    @Override
    public @NotNull Future<PathResult> pathfind(@NotNull Agent agent, @NotNull Vec3I destination) {
        return new ResultFuture(agent, destination);
    }
}
