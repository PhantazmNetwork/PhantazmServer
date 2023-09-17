package org.phantazm.zombies.listener;

import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class PhantazmMobEventListener<TEvent extends EntityInstanceEvent> implements Consumer<TEvent> {
    protected final Instance instance;
    private final Supplier<ZombiesScene> scene;

    public PhantazmMobEventListener(@NotNull Instance instance, @NotNull Supplier<ZombiesScene> scene) {
        this.instance = Objects.requireNonNull(instance);
        this.scene = Objects.requireNonNull(scene);
    }

    @Override
    public void accept(TEvent event) {
        if (event.getInstance() != instance) {
            return;
        }

        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }

        ZombiesScene zombiesScene = scene.get();
        zombiesScene.getAcquirable().sync(self -> accept(self, mob, event));
    }

    protected abstract void accept(@NotNull ZombiesScene scene, @NotNull Mob mob, @NotNull TEvent event);
}
