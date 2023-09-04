package org.phantazm.core.npc;

import net.minestom.server.Tickable;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class NPCHandler implements Tickable {
    private final List<NPC> npcs;
    private final Instance instance;
    private final EventNode<InstanceEvent> instanceNode;

    public NPCHandler(@NotNull List<NPC> npcs, @NotNull Instance instance,
        @NotNull EventNode<InstanceEvent> instanceNode) {
        this.npcs = List.copyOf(npcs);
        this.instance = Objects.requireNonNull(instance);
        this.instanceNode = Objects.requireNonNull(instanceNode);

        instanceNode.addListener(PlayerEntityInteractEvent.class, this::entityInteractEvent);
    }

    private void entityInteractEvent(PlayerEntityInteractEvent event) {
        if (event.getHand() != Player.Hand.MAIN) {
            return;
        }

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
    }

    @Override
    public void tick(long time) {
        for (NPC npc : npcs) {
            npc.tick(time);
        }
    }

    public @NotNull EventNode<InstanceEvent> instanceNode() {
        return instanceNode;
    }
}
