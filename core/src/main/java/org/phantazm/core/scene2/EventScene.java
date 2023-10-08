package org.phantazm.core.scene2;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;

public interface EventScene extends Scene {
    @NotNull EventNode<? super Event> sceneNode();
}
