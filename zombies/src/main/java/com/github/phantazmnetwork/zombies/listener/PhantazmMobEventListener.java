package com.github.phantazmnetwork.zombies.listener;

import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.mob.PhantazmMob;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public abstract class PhantazmMobEventListener<TEvent extends EntityInstanceEvent> implements Consumer<TEvent> {

    private final Instance instance;

    private final MobStore mobStore;

    public PhantazmMobEventListener(@NotNull Instance instance, @NotNull MobStore mobStore) {
        this.instance = Objects.requireNonNull(instance, "instance");
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
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
