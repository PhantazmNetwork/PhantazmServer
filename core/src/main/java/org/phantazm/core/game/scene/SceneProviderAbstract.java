package org.phantazm.core.game.scene;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerProcess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.core.game.scene.event.SceneShutdownEvent;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.StampedLock;

/**
 * An abstract base for {@link SceneProvider}s.
 */
public abstract class SceneProviderAbstract<TScene extends Scene<TRequest>, TRequest extends SceneJoinRequest>
        implements SceneProvider<TScene, TRequest> {
    private final Collection<TScene> scenes = new CopyOnWriteArrayList<>();
    private final Collection<TScene> unmodifiableScenes = Collections.unmodifiableCollection(scenes);
    private final StampedLock lock = new StampedLock();
    private final Executor executor;
    private final int maximumScenes;

    /**
     * Creates an abstract {@link SceneProvider}.
     *
     * @param maximumScenes The maximum number of {@link Scene}s in the provider.
     */
    public SceneProviderAbstract(@NotNull Executor executor, int maximumScenes) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.maximumScenes = maximumScenes;
    }

    @Override
    public @NotNull CompletableFuture<Optional<TScene>> provideScene(@NotNull TRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            long optimisticReadStamp = lock.tryOptimisticRead();
            if (lock.validate(optimisticReadStamp)) {
                Optional<TScene> sceneOptional = chooseScene(request);

                if (lock.validate(optimisticReadStamp) && sceneOptional.isPresent()) {
                    return sceneOptional;
                }
            }

            long writeStamp = lock.writeLock();

            try {
                Optional<TScene> sceneOptional = chooseScene(request);
                if (sceneOptional.isPresent()) {
                    return sceneOptional;
                }

                if (scenes.size() >= maximumScenes) {
                    return Optional.empty();
                }

                TScene scene = createScene(request).join();
                scenes.add(scene);

                return Optional.ofNullable(scene);
            }
            finally {
                lock.unlockWrite(writeStamp);
            }
        }, executor);
    }

    @Override
    public @UnmodifiableView @NotNull Collection<TScene> getScenes() {
        return unmodifiableScenes;
    }

    @Override
    public void forceShutdown() {
        for (TScene scene : scenes) {
            scene.shutdown();
        }

        scenes.clear();
    }

    @Override
    public void tick(long time) {
        Iterator<TScene> iterator = scenes.iterator();

        while (iterator.hasNext()) {
            TScene scene = iterator.next();

            if (scene.isShutdown()) {
                cleanupScene(scene);
                iterator.remove();

                ServerProcess process = MinecraftServer.process();
                if (process != null) {
                    process.eventHandler().call(new SceneShutdownEvent(scene));
                }
            }
            else {
                scene.tick(time);
            }
        }
    }

    /**
     * Chooses a {@link Scene} to be used for a request. This should already be a {@link Scene} in the provider.
     *
     * @param request The request used to choose a {@link Scene}
     * @return An optional of a chosen {@link Scene}
     */
    protected abstract @NotNull Optional<TScene> chooseScene(@NotNull TRequest request);

    /**
     * Creates a {@link Scene}.
     *
     * @param request The join request which triggered the creation of the {@link Scene}
     * @return The new {@link Scene}
     */
    protected abstract @NotNull CompletableFuture<TScene> createScene(@NotNull TRequest request);

    protected abstract void cleanupScene(@NotNull TScene scene);

    @Override
    public boolean hasActiveScenes() {
        for (TScene scene : scenes) {
            if (scene.getIngamePlayerCount() > 0) {
                return true;
            }
        }

        return false;
    }
}
