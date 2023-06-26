package org.phantazm.core;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.InstanceUnregisterEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Standard implementation of {@link ClientBlockHandlerSource}.
 */
public class BasicClientBlockHandlerSource implements ClientBlockHandlerSource {
    private final Function<? super Instance, ? extends ClientBlockHandler> blockHandlerFunction;
    private final Map<UUID, ClientBlockHandler> map;

    /**
     * Creates a new instance of this class given the provided {@link ClientBlockHandler}-producing function. This
     * function must never return null.
     *
     * @param handlerFunction the handler function, which should never return null
     */
    public BasicClientBlockHandlerSource(
            @NotNull Function<? super Instance, ? extends ClientBlockHandler> handlerFunction,
            @NotNull EventNode<Event> globalNode) {
        this.blockHandlerFunction = Objects.requireNonNull(handlerFunction, "handlerFunction");
        this.map = new ConcurrentHashMap<>();

        globalNode.addListener(InstanceUnregisterEvent.class, this::onInstanceUnregister);
    }

    @Override
    public @NotNull ClientBlockHandler forInstance(@NotNull Instance instance) {
        return map.computeIfAbsent(instance.getUniqueId(), ignored -> {
            if (!instance.isRegistered()) {
                throw new IllegalArgumentException("Cannot hold an unregistered instance");
            }

            return Objects.requireNonNull(this.blockHandlerFunction.apply(instance), "handler");
        });
    }

    private void onInstanceUnregister(InstanceUnregisterEvent event) {
        map.remove(event.getInstance().getUniqueId());
    }
}
