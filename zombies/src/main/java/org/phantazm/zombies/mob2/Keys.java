package org.phantazm.zombies.mob2;

import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.map.objects.MapObjects;

public final class Keys {
    private Keys() {
        throw new UnsupportedOperationException();
    }

    public static InjectionStore.Key<MapObjects> MAP_OBJECTS = InjectionStore.key(MapObjects.class);
}
