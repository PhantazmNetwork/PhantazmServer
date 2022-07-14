package com.github.phantazmnetwork.zombies.mapeditor.client;

import com.github.phantazmnetwork.zombies.map.FileSystemMapLoader;
import com.github.phantazmnetwork.zombies.mapeditor.client.render.ObjectRenderer;
import com.github.phantazmnetwork.zombies.mapeditor.client.ui.MainGui;
import com.github.phantazmnetwork.zombies.mapeditor.client.ui.NewObjectGui;
import com.github.steanky.ethylene.codec.yaml.YamlCodec;
import com.github.steanky.ethylene.core.codec.ConfigCodec;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
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
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.key.Key;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * {@link ClientModInitializer} for the map editor.
 */
public class MapeditorClient implements ClientModInitializer {
    /**
     * The path string, relative to the Fabric configuration dir, which points to the editor folder.
     */
    private static final String MAPEDITOR_PATH = "mapeditor";

    @Override
    public void onInitializeClient() {
        ConfigCodec codec = new YamlCodec();
        Path defaultMapDirectory = FabricLoader.getInstance().getConfigDir().resolve(MAPEDITOR_PATH);

        ObjectRenderer renderer = new Renderer();
        Events.registerEventHandlerClass(renderer);

        EditorSession editorSession = new BasicEditorSession(renderer, new FileSystemMapLoader(
                defaultMapDirectory, codec), defaultMapDirectory);
        editorSession.loadMapsFromDisk();

        UseBlockCallback.EVENT.register(editorSession::handleBlockUse);

        KeyBinding mapeditorBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(TranslationKeys
                .KEY_MAPEDITOR_CONFIG, InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M, TranslationKeys.CATEGORY_MAPEDITOR_ALL));
        KeyBinding newObject = KeyBindingHelper.registerKeyBinding(new KeyBinding(TranslationKeys.KEY_MAPEDITOR_CREATE,
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_N, TranslationKeys.CATEGORY_MAPEDITOR_ALL));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(mapeditorBinding.wasPressed()) {
                MinecraftClient.getInstance().setScreen(new CottonClientScreen(new MainGui(editorSession)));
            }
            else if(newObject.wasPressed()) {
                ClientPlayerEntity player = client.player;
                if(player == null) {
                    return;
                }

                if(!editorSession.hasMap()) {
                    player.sendMessage(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_FEEDBACK_NO_ACTIVE_MAP),
                            true);
                    return;
                }

                if(!editorSession.hasSelection()) {
                    player.sendMessage(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_FEEDBACK_NO_SELECTION),
                            true);
                    return;
                }

                MinecraftClient.getInstance().setScreen(new CottonClientScreen(new NewObjectGui(editorSession)));
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
                    //don't render objects whose rendering is disabled
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
            if(renderObjects.remove(key) != null) {
                baked = null;
            }
        }

        @Override
        public void removeIf(@NotNull Predicate<? super Key> keyPredicate) {
            Objects.requireNonNull(keyPredicate, "keyPredicate");
            if(renderObjects.keySet().removeIf(keyPredicate)) {
                baked = null;
            }
        }

        @Override
        public void forEach(@NotNull Consumer<? super RenderObject> consumer) {
            Objects.requireNonNull(consumer, "object");
            for(RenderObject sample : values) {
                consumer.accept(sample);
            }
        }

        @Override
        public void putObject(@NotNull RenderObject value) {
            Objects.requireNonNull(value, "value");

            RenderObject oldObject = renderObjects.get(value.key);
            if(oldObject != null) {
                if(baked != null) {
                    //to avoid having to re-bake, we can find the object in the render array and update in-place
                    int i = 0;
                    for(RenderObject object : baked) {
                        if(object.key.equals(value.key)) {
                            if(oldObject != value) {
                                //update the object present in the map only if necessary
                                renderObjects.put(value.key, value);
                            }

                            baked[i] = value;
                            return;
                        }

                        i++;
                    }

                    //should never fail to find the object, but in case we do, falling through is acceptable
                }
            }

            renderObjects.put(value.key, value);
            baked = null;
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
