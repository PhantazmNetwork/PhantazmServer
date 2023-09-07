package org.phantazm.zombies.equipment.gun2;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.phantazm.commons.InjectionStore;

public class Keys {

    public static final InjectionStore.Key<EventNodeHolder> EVENT_NODE_HOLDER = InjectionStore.key(EventNodeHolder.class);

    public static final InjectionStore.Key<GunModule> GUN_MODULE = InjectionStore.key(GunModule.class);

    private Keys() {
        throw new UnsupportedOperationException();
    }

    // TODO: move
    public record EventNodeHolder(EventNode<Event> eventNode) {

    }

}
