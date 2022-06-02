package com.github.phantazmnetwork.zombies.mapeditor.client;

import com.github.phantazmnetwork.zombies.mapeditor.client.render.ObjectRenderer;
import com.github.phantazmnetwork.zombies.mapeditor.client.ui.MainGui;
import com.github.phantazmnetwork.zombies.mapeditor.client.ui.MapeditorScreen;
import me.x150.renderer.event.EventListener;
import me.x150.renderer.event.EventType;
import me.x150.renderer.event.Events;
import me.x150.renderer.event.Shift;
import me.x150.renderer.event.events.RenderEvent;
import me.x150.renderer.renderer.Renderer3d;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.kyori.adventure.key.Key;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class MapeditorClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ObjectRenderer renderer = new Renderer();
        Events.registerEventHandlerClass(renderer);

        MapeditorSession mapeditorSession = new BasicMapeditorSession(renderer);
        UseBlockCallback.EVENT.register(mapeditorSession::handleBlockUse);

        KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                TranslationKeys.KEY_MAPEDITOR_CONFIG, InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M, TranslationKeys
                .CATEGORY_MAPEDITOR_ALL));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(keyBinding.wasPressed()) {
                MinecraftClient.getInstance().setScreen(new MapeditorScreen(new MainGui(mapeditorSession)));
            }
        });
    }

    private static class Renderer implements ObjectRenderer {
        private static final RenderObject[] EMPTY_RENDER_OBJECT_ARRAY = new RenderObject[0];
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
                if(!object.shouldRender) {
                    continue;
                }

                boolean resetWallRender = false;
                if(!renderThroughWalls && object.renderThroughWalls) {
                    Renderer3d.startRenderingThroughWalls();
                    resetWallRender = true;
                }

                switch (object.type) {
                    case FILLED -> {
                        for(int i = 0; i < object.bounds.length; i += 2) {
                            Renderer3d.renderFilled(stack, object.bounds[i], object.bounds[i + 1], object.color);
                        }
                    }
                    case OUTLINE -> {
                        for(int i = 0; i < object.bounds.length; i += 2) {
                            Renderer3d.renderOutline(stack, object.bounds[i], object.bounds[i + 1], object.color);
                        }
                    }
                }

                if(resetWallRender) {
                    Renderer3d.stopRenderingThroughWalls();
                }
            }

            Renderer3d.stopRenderingThroughWalls();
        }

        private void bake() {
            baked = values.toArray(EMPTY_RENDER_OBJECT_ARRAY);
        }

        @Override
        public void removeObject(@NotNull Key key) {
            Objects.requireNonNull(key, "key");

            renderObjects.remove(key);
            baked = null;
        }

        @Override
        public void putObject(@NotNull RenderObject value) {
            Objects.requireNonNull(value, "value");

            renderObjects.put(value.key, value);

            if(baked != null) {
                if(baked.length == 0) {
                    baked = null;
                    return;
                }

                int i = 0;
                for(RenderObject object : baked) {
                    if(object.key.equals(value.key)) {
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
        public boolean hasObject(@NotNull Key key) {
            return renderObjects.containsKey(key);
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public int size() {
            return renderObjects.size();
        }

        @Override
        public void clear() {
            renderObjects.clear();
            baked = null;
        }
    }
}
