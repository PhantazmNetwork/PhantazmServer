package com.github.phantazmnetwork.api.entity.fakeplayer;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MinimalFakePlayer extends Entity {

    public MinimalFakePlayer(@NotNull UUID uuid) {
        super(EntityType.PLAYER, uuid);
    }

    public MinimalFakePlayer() {
        super(EntityType.PLAYER);
    }

}
