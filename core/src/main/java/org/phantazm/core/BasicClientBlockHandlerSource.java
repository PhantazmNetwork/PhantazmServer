package org.phantazm.core;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.InstanceUnregisterEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Standard implementation of {@link ClientBlockHandlerSource}.
 */
public class BasicClientBlockHandlerSource implements ClientBlockHandlerSource {
    private final Map<UUID, Node> map;

    public BasicClientBlockHandlerSource() {
        this.map = new ConcurrentHashMap<>();

        MinecraftServer.getGlobalEventHandler().addListener(InstanceUnregisterEvent.class, this::onInstanceUnregister);
    }

    @Override
    public @NotNull ClientBlockHandler forInstance(@NotNull Instance instance) {
        return map.computeIfAbsent(instance.getUniqueId(), uuid -> {
            if (!instance.isRegistered()) {
                throw new IllegalArgumentException("Cannot hold an unregistered instance");
            }

            EventNode<InstanceEvent> instanceNode = EventNode.type("client_block_handler_" + uuid,
                EventFilter.INSTANCE, (e, v) -> v.getUniqueId().equals(uuid));
            DimensionType type = instance.getDimensionType();
            Node node = new Node(new InstanceClientBlockHandler(instance, type.getMinY(), type.getHeight(),
                instanceNode), instanceNode);

            MinecraftServer.getGlobalEventHandler().addChild(instanceNode);

            return node;
        }).handler;
    }

    private void onInstanceUnregister(InstanceUnregisterEvent event) {
        Node node = map.remove(event.getInstance().getUniqueId());
        if (node != null) {
            MinecraftServer.getGlobalEventHandler().removeChild(node.node);
        }
    }

    private record Node(@NotNull ClientBlockHandler handler,
        @NotNull EventNode<InstanceEvent> node) {
    }
}