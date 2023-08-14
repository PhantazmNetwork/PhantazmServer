package org.phantazm.mob2;

import org.phantazm.commons.InjectionStore;

public class Keys {
    private Keys() {
        throw new UnsupportedOperationException();
    }

    public static final InjectionStore.Key<Mob> MOB_KEY = InjectionStore.key(Mob.class);
}
