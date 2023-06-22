package org.phantazm.core.npc;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class NPCHandler {
    private final Instance instance;
    private final List<NPC> npcs;
    private final EventNode<InstanceEvent> instanceEventNode;
    private final EventNode<InstanceEvent> handlerNode;

    public NPCHandler(@NotNull List<NPC> npcs, @NotNull Instance instance) {
        this.instance = Objects.requireNonNull(instance, "instance");
        this.npcs = List.copyOf(npcs);
        this.instanceEventNode = instance.eventNode();

        this.handlerNode = EventNode.event("npc_handler_{" + instance.getUniqueId() + "}",
                EventFilter.from(InstanceEvent.class, Instance.class, InstanceEvent::getInstance), event -> true);

        handlerNode.addListener(PlayerEntityInteractEvent.class, this::entityInteractEvent);
        instanceEventNode.addChild(handlerNode);
    }

    private void entityInteractEvent(PlayerEntityInteractEvent event) {
        if (event.getInstance() != instance) {
            return;
        }

        Entity entity = event.getTarget();
        for (NPC npc : npcs) {
            if (entity.getUuid().equals(npc.uuid())) {
                npc.handleInteraction(event.getPlayer());
            }
        }
    }

    public void spawnAll() {
        for (NPC npc : npcs) {
            npc.spawn(instance);
        }
    }

    public void end() {
        for (NPC npc : npcs) {
            npc.despawn();
        }

        instanceEventNode.removeChild(handlerNode);
    }
}
