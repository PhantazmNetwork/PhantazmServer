package org.phantazm.zombies.autosplits;

import com.github.steanky.ethylene.codec.yaml.YamlCodec;
import com.github.steanky.ethylene.core.BasicConfigHandler;
import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.ConfigHandler;
import com.github.steanky.ethylene.core.loader.ConfigLoader;
import com.github.steanky.ethylene.core.loader.SyncFileConfigLoader;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.phantazm.zombies.autosplits.config.ZombiesAutoSplitsConfig;
import org.phantazm.zombies.autosplits.config.ZombiesAutoSplitsConfigProcessor;
import org.phantazm.zombies.autosplits.event.ClientSoundCallback;
import org.phantazm.zombies.autosplits.packet.RoundStartPacketWrapper;
import org.phantazm.zombies.autosplits.render.RenderTimeHandler;
import org.phantazm.zombies.autosplits.sound.AutoSplitSoundInterceptor;
import org.phantazm.zombies.autosplits.splitter.AutoSplitSplitter;
import org.phantazm.zombies.autosplits.splitter.CompositeSplitter;
import org.phantazm.zombies.autosplits.splitter.internal.InternalSplitter;
import org.phantazm.zombies.autosplits.splitter.socket.LiveSplitSocketSplitter;
import org.phantazm.zombies.autosplits.tick.KeyInputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.*;

public class ZombiesAutoSplitsClient implements ClientModInitializer {

    public static final String MODID = "zombies-autosplits";

    private static ZombiesAutoSplitsClient instance = null;

    private final KeyBinding autoSplitsKeybind =
        new KeyBinding("Toggle AutoSplits", GLFW.GLFW_KEY_SEMICOLON, "Phantazm");

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final Logger logger = LoggerFactory.getLogger(MODID);

    private final ConfigHandler configHandler = new BasicConfigHandler();

    private final ConfigHandler.ConfigKey<ZombiesAutoSplitsConfig> configKey =
        new ConfigHandler.ConfigKey<>(ZombiesAutoSplitsConfig.class, MODID + "_config");

    private final Collection<AutoSplitSplitter> splitters = new CopyOnWriteArrayList<>();

    private RenderTimeHandler renderTimeHandler;

    private ZombiesAutoSplitsConfig config;

    private CompositeSplitter compositeSplitter;

    public static @NotNull ZombiesAutoSplitsClient getInstance() {
        return instance;
    }

    private CompletableFuture<ZombiesAutoSplitsConfig> loadConfigFromFile() {
        return configHandler.writeDefaults().thenCompose((unused) -> configHandler.loadData(configKey));
    }

    public @NotNull ZombiesAutoSplitsConfig getConfig() {
        return config;
    }

    public void setConfig(@NotNull ZombiesAutoSplitsConfig config) {
        for (AutoSplitSplitter splitter : splitters) {
            splitter.cancel();
        }
        splitters.clear();

        this.config = config;

        if (config.useLiveSplits()) {
            splitters.add(new LiveSplitSocketSplitter(executor, config.host(), config.port()));
        }
        if (config.useInternal()) {
            InternalSplitter internalSplitter = new InternalSplitter();
            splitters.add(internalSplitter);
            renderTimeHandler.setSplitter(internalSplitter);
        } else {
            renderTimeHandler.setSplitter(null);
        }
    }

    public void saveConfig() {
        configHandler.writeData(configKey, config).whenComplete((ignored, throwable) -> {
            if (throwable != null) {
                logger.error("Failed to save config", throwable);
            }
        });
    }

    @Override
    public void onInitializeClient() {
        renderTimeHandler = new RenderTimeHandler(MinecraftClient.getInstance(), 0xFFFFFF);

        initConfig();

        compositeSplitter = new CompositeSplitter(MinecraftClient.getInstance(), logger, splitters);

        initEvents();
        initKeyBindings();
        initPackets();

        instance = this;
    }

    private void initConfig() {
        ConfigCodec codec = new YamlCodec();
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("zombies-autosplits");
        String configFileName;
        if (codec.getPreferredExtensions().isEmpty()) {
            configFileName = "config";
        } else {
            configFileName = "config." + codec.getPreferredExtension();
        }

        try {
            Files.createDirectories(configPath);
        } catch (IOException e) {
            logger.error("Failed to create config directory", e);
            return;
        }

        Path configFile = configPath.resolve(configFileName);
        ZombiesAutoSplitsConfig defaultConfig = ZombiesAutoSplitsConfig.DEFAULT;
        ConfigProcessor<ZombiesAutoSplitsConfig> configProcessor = new ZombiesAutoSplitsConfigProcessor();
        ConfigLoader<ZombiesAutoSplitsConfig> configLoader =
            new SyncFileConfigLoader<>(configProcessor, defaultConfig, configFile, codec);
        configHandler.registerLoader(configKey, configLoader);

        ZombiesAutoSplitsConfig loadedConfig = loadConfigFromFile().join();
        setConfig(loadedConfig);
    }

    private void initEvents() {
        AutoSplitSoundInterceptor soundInterceptor =
            new AutoSplitSoundInterceptor(MinecraftClient.getInstance(), compositeSplitter);
        ClientTickEvents.END_CLIENT_TICK.register(new KeyInputHandler(autoSplitsKeybind, compositeSplitter));
        HudRenderCallback.EVENT.register(renderTimeHandler);
        ClientSoundCallback.EVENT.register(soundInterceptor);
        ClientLifecycleEvents.CLIENT_STOPPING.register(unused -> shutdownExecutor());
    }

    private void shutdownExecutor() {
        logger.info("Shutting down executor");
        executor.shutdown();
        try {
            if (executor.awaitTermination(5L, TimeUnit.SECONDS)) {
                return;
            }

            logger.warn("Executor did not shut down in 5 seconds. Force shutting down");
            executor.shutdownNow();

            if (!executor.awaitTermination(5L, TimeUnit.SECONDS)) {
                logger.warn("Executor did not terminate");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void initKeyBindings() {
        KeyBindingHelper.registerKeyBinding(autoSplitsKeybind);
    }

    private void initPackets() {
        ClientPlayNetworking.registerGlobalReceiver(RoundStartPacketWrapper.TYPE, (packet, player, responseSender) -> {
            compositeSplitter.split();
        });
    }

}
