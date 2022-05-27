package com.github.phantazmnetwork.zombies.mapeditor.client;

import com.github.phantazmnetwork.zombies.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.mapeditor.client.render.EditorGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Set;

public interface MapeditorSession {
    @NotNull ActionResult handleBlockUse(@NotNull PlayerEntity player, @NotNull World world, @NotNull Hand hand,
                                         @NotNull BlockHitResult blockHitResult);

    boolean isEnabled();

    void setEnabled(boolean enabled);

    boolean hasSelection();

    @NotNull Vec3i getFirstBlock();

    @NotNull Vec3i getSecondBlock();

    boolean hasMap();

    @NotNull ZombiesMap currentMap();

    @NotNull @UnmodifiableView Set<EditorGroup<?>> getRenderGroups();
}
