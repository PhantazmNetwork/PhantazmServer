package com.github.phantazmnetwork.zombies.mapeditor.client;

import com.github.phantazmnetwork.commons.StringConstants;
import com.github.phantazmnetwork.zombies.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.mapeditor.client.render.ObjectRenderer;
import com.github.phantazmnetwork.zombies.mapeditor.client.render.EditorGroup;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.key.Key;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.awt.*;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class BasicMapeditorSession implements MapeditorSession {
    private static final Color SELECTION_COLOR = new Color(0, 255, 0, 128);
    private static final Color CURSOR_COLOR = Color.RED;
    private static final Color OUTLINE_COLOR = Color.BLACK;
    private static final Vec3i ONE = new Vec3i(1, 1, 1);
    private static final Vec3d HALF = new Vec3d(0.5, 0.5, 0.5);
    private static final Key SELECTION_KEY = Key.key(StringConstants.PHANTAZM_NAMESPACE, "mapeditor_selection");
    private static final Key OUTLINE_KEY = Key.key(StringConstants.PHANTAZM_NAMESPACE, "mapeditor_selection_outline");
    private static final Key CURSOR_KEY = Key.key(StringConstants.PHANTAZM_NAMESPACE, "mapeditor_cursor");

    private final ObjectRenderer renderer;

    private Vec3i firstSelection;
    private Vec3i secondSelection;

    private boolean enabled;

    private final Set<EditorGroup<?>> editorGroupsView;

    private ZombiesMap currentMap;

    public BasicMapeditorSession(@NotNull ObjectRenderer renderer, @NotNull Set<EditorGroup<?>> editorGroups) {
        this.renderer = Objects.requireNonNull(renderer, "renderer");
        this.editorGroupsView = Collections.unmodifiableSet(Objects.requireNonNull(editorGroups,
                "renderGroups"));
    }

    @Override
    public @NotNull ActionResult handleBlockUse(@NotNull PlayerEntity player, @NotNull World world, @NotNull Hand hand,
                                                @NotNull BlockHitResult blockHitResult) {
        if(!enabled || !player.getInventory().getMainHandStack().getItem().equals(Items.STICK)) {
            return ActionResult.PASS;
        }

        if(hand == Hand.MAIN_HAND) {
            Vec3i newSelection = blockHitResult.getBlockPos();
            if(firstSelection == null) {
                setSelectionRender(newSelection, ONE, newSelection);
                firstSelection = newSelection;
                secondSelection = newSelection;
            }
            else {
                Vec3i min = new Vec3i(Math.min(newSelection.getX(), firstSelection.getX()), Math.min(newSelection
                        .getY(), firstSelection.getY()), Math.min(newSelection.getZ(), firstSelection.getZ()));
                Vec3i max = new Vec3i(Math.max(newSelection.getX(), firstSelection.getX()), Math.max(newSelection
                        .getY(), firstSelection.getY()), Math.max(newSelection.getZ(), firstSelection.getZ()));
                secondSelection = firstSelection;
                firstSelection = newSelection;

                setSelectionRender(min, max.subtract(min).add(1, 1, 1), newSelection);
            }

            return ActionResult.CONSUME;
        }

        return ActionResult.PASS;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if(enabled != this.enabled) {
            if(!enabled) {
                renderer.setEnabled(false);
                firstSelection = null;
                secondSelection = null;
            }
            else {
                renderer.setEnabled(true);
            }

            this.enabled = enabled;
        }
    }

    @Override
    public boolean hasSelection() {
        return firstSelection != null;
    }

    @Override
    public @NotNull Vec3i getFirstBlock() {
        if(firstSelection == null) {
            throw new IllegalStateException("There is no selection for the current session");
        }

        return firstSelection;
    }

    @Override
    public @NotNull Vec3i getSecondBlock() {
        if(firstSelection == null) {
            throw new IllegalStateException("There is no selection for the current session");
        }

        return secondSelection;
    }

    @Override
    public boolean hasMap() {
        return currentMap != null;
    }

    @Override
    public @NotNull ZombiesMap currentMap() {
        if(currentMap == null) {
            throw new IllegalStateException("There is no map for the current session");
        }

        return currentMap;
    }

    @Override
    public @NotNull @UnmodifiableView Set<EditorGroup<?>> getRenderGroups() {
        return editorGroupsView;
    }

    private void setSelectionRender(Vec3i areaStart, Vec3i dimensions, Vec3i clicked) {
        Vec3d startVec = new Vec3d(areaStart.getX() - ObjectRenderer.EPSILON, areaStart.getY() - ObjectRenderer
                .EPSILON, areaStart.getZ() - ObjectRenderer.EPSILON);
        Vec3d dimensionsVec = new Vec3d(dimensions.getX() + ObjectRenderer.DOUBLE_EPSILON, dimensions.getY() +
                ObjectRenderer.DOUBLE_EPSILON, dimensions.getZ() + ObjectRenderer.DOUBLE_EPSILON);
        Vec3d clickedVec = new Vec3d(clicked.getX(), clicked.getY(), clicked.getZ());

        renderer.putObject(new ObjectRenderer.RenderObject(SELECTION_KEY, ObjectRenderer.RenderType.FILLED,
                SELECTION_COLOR, true, false, startVec, dimensionsVec));
        renderer.putObject(new ObjectRenderer.RenderObject(OUTLINE_KEY, ObjectRenderer.RenderType.OUTLINE,
                OUTLINE_COLOR, true, false, startVec, dimensionsVec));
        renderer.putObject(new ObjectRenderer.RenderObject(CURSOR_KEY, ObjectRenderer.RenderType.OUTLINE, CURSOR_COLOR,
                true, true, clickedVec.add(0.25, 0.25, 0.25),
                HALF));
    }
}
