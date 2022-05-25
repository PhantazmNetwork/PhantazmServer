package com.github.phantazmnetwork.zombies.mapeditor.client;

import me.x150.renderer.event.EventListener;
import me.x150.renderer.event.EventType;
import me.x150.renderer.event.Events;
import me.x150.renderer.event.Shift;
import me.x150.renderer.event.events.RenderEvent;
import me.x150.renderer.renderer.Renderer3d;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayDeque;

public class MapeditorClient implements ClientModInitializer {
    enum RenderType {
        FILLED,
        OUTLINE
    }

    private record RenderObject(RenderType type, Vec3d start, Vec3d end, Color color) {}


    @Override
    public void onInitializeClient() {
        Events.registerEventHandlerClass(new Renderer());

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            return ActionResult.PASS;
        });
    }

    private static class Renderer {
        private static boolean renderThroughWalls = false;
        private static RenderObject[] renderObjects = new RenderObject[16];

        @EventListener(shift = Shift.POST, type = EventType.WORLD_RENDER)
        void worldRender(RenderEvent event) {
            MatrixStack stack = event.getStack();
            if(renderThroughWalls) {
                Renderer3d.startRenderingThroughWalls();
            }

            for(RenderObject object : renderObjects) {
                switch (object.type) {
                    case FILLED -> Renderer3d.renderFilled(stack, object.start, object.end, object.color);
                    case OUTLINE -> Renderer3d.renderOutline(stack, object.start, object.end, object.color);
                }
            }

            Renderer3d.stopRenderingThroughWalls();
        }
    }
}
