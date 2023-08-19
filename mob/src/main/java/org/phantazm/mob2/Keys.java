package org.phantazm.mob2;

import org.phantazm.commons.InjectionStore;

public final class Keys {
    public static InjectionStore.Key<MobSpawner> MOB_SPAWNER = InjectionStore.key(MobSpawner.class);

    private Keys() {
        throw new UnsupportedOperationException();
    }
}
