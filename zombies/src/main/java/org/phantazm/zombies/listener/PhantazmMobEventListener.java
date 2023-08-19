package org.phantazm.zombies.listener;

import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.PhantazmMob;

import java.util.Objects;
import java.util.function.Consumer;

public abstract class PhantazmMobEventListener<TEvent extends EntityInstanceEvent> implements Consumer<TEvent> {
    protected final Instance instance;
    protected final MobStore mobStore;

    public PhantazmMobEventListener(@NotNull Instance instance, @NotNull MobStore mobStore) {
        this.instance = Objects.requireNonNull(instance);
        this.mobStore = Objects.requireNonNull(mobStore);
    }

    @Override
    public void accept(TEvent event) {
        if (event.getInstance() != instance) {
            return;
        }
        PhantazmMob mob = mobStore.getMob(event.getEntity().getUuid());
        if (mob != null) {
            accept(mob, event);
        }
    }

    protected abstract void accept(@NotNull PhantazmMob mob, @NotNull TEvent event);

    protected @NotNull MobStore getMobStore() {
        return mobStore;
    }
}
