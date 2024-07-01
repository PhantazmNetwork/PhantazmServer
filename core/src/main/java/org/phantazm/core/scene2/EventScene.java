package org.phantazm.core.scene2;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface EventScene extends Scene {
    @NotNull EventNode<? super Event> sceneNode();

    default void broadcastEvent(@NotNull Event event) {
        sceneNode().call(event);
    }

    default <T extends CancellableEvent> void broadcastCancellable(@NotNull T event, @NotNull Consumer<? super T> callback) {
        sceneNode().call(event);
        if (!event.isCancelled()) {
            callback.accept(event);
        }
    }

    default <E extends Event> void addListener(@NotNull Class<E> eventClass, @NotNull Consumer<E> listener) {
        sceneNode().addListener(eventClass, listener);
    }
}
