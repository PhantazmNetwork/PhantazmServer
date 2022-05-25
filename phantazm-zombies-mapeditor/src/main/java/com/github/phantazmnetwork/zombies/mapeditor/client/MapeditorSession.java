package com.github.phantazmnetwork.zombies.mapeditor.client;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public interface MapeditorSession {
    @NotNull ActionResult handleBlockUse(@NotNull PlayerEntity player, @NotNull World world, @NotNull Hand hand,
                                         @NotNull BlockHitResult blockHitResult);
}
