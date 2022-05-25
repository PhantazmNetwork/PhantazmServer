package com.github.phantazmnetwork.zombies.mapeditor.client;

import com.github.phantazmnetwork.zombies.mapeditor.client.render.ObjectRenderer;
import me.x150.renderer.event.EventListener;
import me.x150.renderer.event.EventType;
import me.x150.renderer.event.Events;
import me.x150.renderer.event.Shift;
import me.x150.renderer.event.events.RenderEvent;
import me.x150.renderer.renderer.Renderer3d;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
public class MapeditorClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ObjectRenderer renderer = new Renderer();
        Events.registerEventHandlerClass(renderer);
        UseBlockCallback.EVENT.register(new BasicMapeditorSession(renderer)::handleBlockUse);
    }

    private static class Renderer implements ObjectRenderer {
        private boolean renderThroughWalls = false;

        private final ArrayList<RenderObject> renderObjects = new ArrayList<>();
        private RenderObject[] baked;

        @EventListener(shift = Shift.POST, type = EventType.WORLD_RENDER)
        void worldRender(RenderEvent event) {
            MatrixStack stack = event.getStack();
            if(renderThroughWalls) {
                Renderer3d.startRenderingThroughWalls();
            }

            if(baked == null) {
                bake();
            }

            for(ObjectRenderer.RenderObject object : baked) {
                switch (object.type()) {
                    case FILLED -> Renderer3d.renderFilled(stack, object.start(), object.end(), object.color());
                    case OUTLINE -> Renderer3d.renderOutline(stack, object.start(), object.end(), object.color());
                }
            }

            Renderer3d.stopRenderingThroughWalls();
        }

        private void bake() {
            baked = renderObjects.toArray(new RenderObject[0]);
        }

        @Override
        public void addObject(@NotNull RenderObject object) {
            renderObjects.add(object);
            baked = null;
        }

        @Override
        public void removeObject(int index) {
            renderObjects.remove(index);
            baked = null;
        }

        @Override
        public void setObject(int index, @NotNull RenderObject value) {
            renderObjects.set(index, value);
            baked[index] = value;
        }

        @Override
        public void setRenderThroughWalls(boolean renderThroughWalls) {
            this.renderThroughWalls = renderThroughWalls;
        }
    }
}
