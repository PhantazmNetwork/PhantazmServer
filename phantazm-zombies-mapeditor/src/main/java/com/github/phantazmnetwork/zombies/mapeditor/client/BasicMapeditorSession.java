package com.github.phantazmnetwork.zombies.mapeditor.client;

import com.github.phantazmnetwork.zombies.mapeditor.client.render.ObjectRenderer;
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

import java.awt.*;
import java.util.Objects;

public class BasicMapeditorSession implements MapeditorSession {
    private static final Color SELECTION_COLOR = new Color(0, 255, 0, 128);
    private static final Color CURSOR_COLOR = Color.RED;
    private static final Color OUTLINE_COLOR = Color.BLACK;
    private static final float EPSILON = 1E-3F;
    private static final float DOUBLE_EPSILON = EPSILON * 2;
    private static final Vec3i ONE = new Vec3i(1, 1, 1);
    private static final Vec3d HALF = new Vec3d(0.5, 0.5, 0.5);

    private final ObjectRenderer renderer;
    private final Key selectionKey;
    private final Key outlineKey;
    private final Key cursorKey;

    private Vec3i firstSelection;
    private Vec3i secondSelection;

    public BasicMapeditorSession(@NotNull ObjectRenderer renderer) {
        this.renderer = Objects.requireNonNull(renderer, "renderer");
        this.selectionKey = Key.key("phantazm", "mapeditor_selection");
        this.outlineKey = Key.key("phantazm", "mapeditor_selection_outline");
        this.cursorKey = Key.key("phantazm", "mapeditor_cursor");
        renderer.setEnabled(true);
    }

    @Override
    public @NotNull ActionResult handleBlockUse(@NotNull PlayerEntity player, @NotNull World world, @NotNull Hand hand,
                                                @NotNull BlockHitResult blockHitResult) {
        System.out.println(player.getInventory().getMainHandStack().getItem());
        if(!renderer.isEnabled() || !player.getInventory().getMainHandStack().getItem().equals(Items.STICK)) {
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

                System.out.println(min);
                System.out.println(max);

                secondSelection = firstSelection;
                firstSelection = newSelection;

                setSelectionRender(min, max.subtract(min).add(1, 1, 1), newSelection);
            }

            return ActionResult.CONSUME;
        }

        return ActionResult.PASS;
    }

    private void setSelectionRender(Vec3i areaStart, Vec3i dimensions, Vec3i clicked) {
        Vec3d startVec = new Vec3d(areaStart.getX(), areaStart.getY(), areaStart.getZ());
        Vec3d dimensionsVec = new Vec3d(dimensions.getX(), dimensions.getY(), dimensions.getZ());
        Vec3d clickedVec = new Vec3d(clicked.getX(), clicked.getY(), clicked.getZ());

        renderer.setObject(new ObjectRenderer.RenderObject(selectionKey, ObjectRenderer.RenderType.FILLED, startVec
                .subtract(EPSILON, EPSILON, EPSILON), dimensionsVec.add(DOUBLE_EPSILON, DOUBLE_EPSILON, DOUBLE_EPSILON),
                SELECTION_COLOR, false));
        renderer.setObject(new ObjectRenderer.RenderObject(outlineKey, ObjectRenderer.RenderType.OUTLINE, startVec
                .subtract(EPSILON, EPSILON, EPSILON), dimensionsVec.add(DOUBLE_EPSILON, DOUBLE_EPSILON, DOUBLE_EPSILON),
                OUTLINE_COLOR, false));
        renderer.setObject(new ObjectRenderer.RenderObject(cursorKey, ObjectRenderer.RenderType.OUTLINE, clickedVec
                .add(0.25, 0.25, 0.25), HALF, CURSOR_COLOR, true));
    }
}
