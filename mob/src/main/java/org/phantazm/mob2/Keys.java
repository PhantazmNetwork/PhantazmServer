package org.phantazm.mob2;

import net.minestom.server.timer.Scheduler;
import org.phantazm.commons.InjectionStore;

public final class Keys {
    public static InjectionStore.Key<MobSpawner> MOB_SPAWNER = InjectionStore.key(MobSpawner.class);

    public static InjectionStore.Key<Scheduler> SCHEDULER = InjectionStore.key(Scheduler.class);

    private Keys() {
        throw new UnsupportedOperationException();
    }
}
