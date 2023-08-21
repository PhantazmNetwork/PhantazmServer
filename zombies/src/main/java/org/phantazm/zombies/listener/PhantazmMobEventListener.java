package org.phantazm.zombies.listener;

import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;

import java.util.Objects;
import java.util.function.Consumer;

public abstract class PhantazmMobEventListener<TEvent extends EntityInstanceEvent> implements Consumer<TEvent> {
    protected final Instance instance;

    public PhantazmMobEventListener(@NotNull Instance instance) {
        this.instance = Objects.requireNonNull(instance);
    }

    @Override
    public void accept(TEvent event) {
        if (event.getInstance() != instance) {
            return;
        }

        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }

        accept(mob, event);
    }

    protected abstract void accept(@NotNull Mob mob, @NotNull TEvent event);
}
