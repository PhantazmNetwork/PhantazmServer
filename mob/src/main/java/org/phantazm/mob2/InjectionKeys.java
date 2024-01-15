package org.phantazm.mob2;

import net.minestom.server.timer.Scheduler;
import org.phantazm.commons.InjectionStore;

public final class InjectionKeys {
    public static InjectionStore.Key<MobSpawner> MOB_SPAWNER = InjectionStore.key(MobSpawner.class);

    public static InjectionStore.Key<Scheduler> SCHEDULER = InjectionStore.key(Scheduler.class);

    private InjectionKeys() {
        throw new UnsupportedOperationException();
    }
}
