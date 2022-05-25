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
import net.kyori.adventure.key.Key;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MapeditorClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ObjectRenderer renderer = new Renderer();
        Events.registerEventHandlerClass(renderer);
        UseBlockCallback.EVENT.register(new BasicMapeditorSession(renderer)::handleBlockUse);
    }

    private static class Renderer implements ObjectRenderer {
        private boolean enabled;
        private boolean renderThroughWalls = false;
        private final Map<Key, RenderObject> renderObjects = new HashMap<>();
        private final Collection<RenderObject> values = renderObjects.values();
        private RenderObject[] baked;

        @EventListener(shift = Shift.POST, type = EventType.WORLD_RENDER)
        void worldRender(@NotNull RenderEvent event) {
            if(!enabled) {
                return;
            }

            MatrixStack stack = event.getStack();
            if(renderThroughWalls) {
                Renderer3d.startRenderingThroughWalls();
            }

            if(baked == null) {
                bake();
            }

            for(ObjectRenderer.RenderObject object : baked) {
                boolean resetWallRender = false;
                if(!renderThroughWalls && object.renderThroughWalls()) {
                    Renderer3d.startRenderingThroughWalls();
                    resetWallRender = true;
                }

                switch (object.type()) {
                    case FILLED -> Renderer3d.renderFilled(stack, object.start(), object.dimensions(), object.color());
                    case OUTLINE -> Renderer3d.renderOutline(stack, object.start(), object.dimensions(), object.color());
                }

                if(resetWallRender) {
                    Renderer3d.stopRenderingThroughWalls();
                }
            }

            Renderer3d.stopRenderingThroughWalls();
        }

        private void bake() {
            baked = values.toArray(new RenderObject[0]);
        }

        @Override
        public void removeObject(@NotNull Key key) {
            renderObjects.remove(key);
            baked = null;
        }

        @Override
        public void setObject(@NotNull RenderObject value) {
            renderObjects.put(value.key(), value);

            if(baked != null) {
                if(baked.length == 0) {
                    baked = null;
                    return;
                }

                int i = 0;

                for(RenderObject object : baked) {
                    if(object.key().equals(value.key())) {
                        baked[i] = value;
                        return;
                    }

                    i++;
                }
            }
        }

        @Override
        public void setRenderThroughWalls(boolean renderThroughWalls) {
            this.renderThroughWalls = renderThroughWalls;
        }

        @Override
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public int size() {
            return renderObjects.size();
        }
    }
}
