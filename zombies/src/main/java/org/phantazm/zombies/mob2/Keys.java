package org.phantazm.zombies.mob2;

import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.scene2.ZombiesScene;

public final class Keys {
    public static InjectionStore.Key<ZombiesScene> SCENE = InjectionStore.key(ZombiesScene.class);

    private Keys() {
        throw new UnsupportedOperationException();
    }
}
