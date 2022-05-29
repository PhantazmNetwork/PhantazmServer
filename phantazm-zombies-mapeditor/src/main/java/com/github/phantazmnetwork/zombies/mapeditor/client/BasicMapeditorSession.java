package com.github.phantazmnetwork.zombies.mapeditor.client;

import com.github.phantazmnetwork.commons.StringConstants;
import com.github.phantazmnetwork.commons.databind.Property;
import com.github.phantazmnetwork.zombies.map.MapInfo;
import com.github.phantazmnetwork.zombies.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.mapeditor.client.render.ObjectRenderer;
import com.github.phantazmnetwork.zombies.mapeditor.client.ui.MapeditorViewModel;
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
    private static final Vec3i ONE = new Vec3i(1, 1, 1);
    private static final Vec3d HALF = new Vec3d(0.5, 0.5, 0.5);
    private static final Key SELECTION_KEY = Key.key(StringConstants.PHANTAZM_NAMESPACE, "mapeditor_selection");
    private static final Key OUTLINE_KEY = Key.key(StringConstants.PHANTAZM_NAMESPACE, "mapeditor_selection_outline");
    private static final Key CURSOR_KEY = Key.key(StringConstants.PHANTAZM_NAMESPACE, "mapeditor_cursor");

    private final ObjectRenderer renderer;
    private final MapeditorViewModel viewModel;

    private ZombiesMap currentMap;

    public BasicMapeditorSession(@NotNull ObjectRenderer renderer) {
        this.renderer = Objects.requireNonNull(renderer, "renderer");
        this.viewModel = new MapeditorViewModel(Property.of(false), Property.of(), Property.of(), Property
                .of());
        this.viewModel.enabled().addListener((oldValue, newValue) -> renderer.setEnabled(newValue));
        this.viewModel.currentMapName().addListener((oldValue, newValue) -> {
            if(newValue == null) {
                currentMap = null;
            }
            else {

            }
        });
    }

    @Override
    public @NotNull ActionResult handleBlockUse(@NotNull PlayerEntity player, @NotNull World world, @NotNull Hand hand,
                                                @NotNull BlockHitResult blockHitResult) {
        if(!viewModel.enabled().get() || !player.getInventory().getMainHandStack().getItem().equals(Items
                .STICK)) {
            return ActionResult.PASS;
        }

        if(hand == Hand.MAIN_HAND) {
            Vec3i firstSelection = viewModel.firstSelected().get();
            Vec3i newSelection = blockHitResult.getBlockPos();

            if(firstSelection == null) {
                viewModel.firstSelected().set(newSelection);
                viewModel.secondSelected().set(newSelection);

                updateSelectionRender(newSelection, ONE, newSelection);
            }
            else {

                Vec3i min = new Vec3i(Math.min(newSelection.getX(), firstSelection.getX()), Math.min(newSelection
                        .getY(), firstSelection.getY()), Math.min(newSelection.getZ(), firstSelection.getZ()));
                Vec3i max = new Vec3i(Math.max(newSelection.getX(), firstSelection.getX()), Math.max(newSelection
                        .getY(), firstSelection.getY()), Math.max(newSelection.getZ(), firstSelection.getZ()));

                viewModel.secondSelected().set(firstSelection);
                viewModel.firstSelected().set(newSelection);

                updateSelectionRender(min, max.subtract(min).add(1, 1, 1), newSelection);
            }

            return ActionResult.CONSUME;
        }

        return ActionResult.PASS;
    }

    @Override
    public @NotNull MapeditorViewModel getViewModel() {
        return viewModel;
    }

    private void updateSelectionRender(Vec3i areaStart, Vec3i dimensions, Vec3i clicked) {
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
                true, true, clickedVec.add(0.25, 0.25, 0.25), HALF));
    }
}
