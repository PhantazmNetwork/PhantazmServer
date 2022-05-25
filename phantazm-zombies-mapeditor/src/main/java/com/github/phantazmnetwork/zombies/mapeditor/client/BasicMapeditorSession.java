package com.github.phantazmnetwork.zombies.mapeditor.client;

import com.github.phantazmnetwork.zombies.mapeditor.client.render.ObjectRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BasicMapeditorSession implements MapeditorSession {
    private final ObjectRenderer renderer;

    public BasicMapeditorSession(@NotNull ObjectRenderer renderer) {
        this.renderer = Objects.requireNonNull(renderer, "renderer");
    }

    @Override
    public @NotNull ActionResult handleBlockUse(@NotNull PlayerEntity player, @NotNull World world, @NotNull Hand hand,
                                                @NotNull BlockHitResult blockHitResult) {
        return null;
    }
}
