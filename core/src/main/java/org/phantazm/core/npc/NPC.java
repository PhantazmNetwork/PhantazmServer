package org.phantazm.core.npc;

import net.minestom.server.Tickable;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface NPC extends Tickable {
    void handleInteraction(@NotNull Player interactor);

    void spawn(@NotNull Instance instance);

    void despawn();

    @Nullable UUID uuid();
}
