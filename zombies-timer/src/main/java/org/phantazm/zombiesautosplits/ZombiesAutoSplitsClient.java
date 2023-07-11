package org.phantazm.zombiesautosplits;

import com.github.steanky.ethylene.codec.yaml.YamlCodec;
import com.github.steanky.ethylene.core.BasicConfigHandler;
import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.ConfigHandler;
import com.github.steanky.ethylene.core.loader.SyncFileConfigLoader;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.phantazm.messaging.packet.PacketHandler;
import org.phantazm.messaging.serialization.PacketSerializer;
import org.phantazm.messaging.serialization.PacketSerializers;
import org.phantazm.zombiesautosplits.config.ZombiesAutoSplitsConfig;
import org.phantazm.zombiesautosplits.config.ZombiesAutoSplitsConfigProcessor;
import org.phantazm.zombiesautosplits.event.ClientSoundCallback;
import org.phantazm.zombiesautosplits.messaging.PhantazmMessagingHandler;
import org.phantazm.zombiesautosplits.packet.PacketByteBufDataReader;
import org.phantazm.zombiesautosplits.packet.PacketByteBufDataWriter;
import org.phantazm.zombiesautosplits.packet.PhantazmPacket;
import org.phantazm.zombiesautosplits.render.RenderTimeHandler;
import org.phantazm.zombiesautosplits.sound.AutoSplitSoundInterceptor;
import org.phantazm.zombiesautosplits.splitter.CompositeSplitter;
import org.phantazm.zombiesautosplits.splitter.LiveSplitSplitter;
import org.phantazm.zombiesautosplits.splitter.internal.InternalSplitter;
import org.phantazm.zombiesautosplits.splitter.socket.LiveSplitSocketSplitter;
import org.phantazm.zombiesautosplits.tick.KeyInputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ZombiesAutoSplitsClient implements ClientModInitializer {

    public static final String MODID = "zombiesautosplits";

    private static ZombiesAutoSplitsClient instance = null;

    private final KeyBinding autoSplitsKeybind =
            new KeyBinding("Toggle AutoSplits", GLFW.GLFW_KEY_SEMICOLON, "Phantazm");

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private final Logger logger = LoggerFactory.getLogger(MODID);

    private final ConfigHandler configHandler = new BasicConfigHandler();

    private final ConfigHandler.ConfigKey<ZombiesAutoSplitsConfig> configKey =
            new ConfigHandler.ConfigKey<>(ZombiesAutoSplitsConfig.class, "zombiesautosplits_config");

    private final Collection<LiveSplitSplitter> splitters = new ArrayList<>(2);

    private RenderTimeHandler renderTimeHandler;

    private ZombiesAutoSplitsConfig config;

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
        for (LiveSplitSplitter splitter : splitters) {
            splitter.cancel();
        }
        splitters.clear();

        this.config = config;

        if (config.useLiveSplits()) {
            splitters.add(new LiveSplitSocketSplitter(executor, config.host(), config.port()));
        }
        if (config.useInternal()) {
            InternalSplitter internalSplitter = new InternalSplitter(executor);
            splitters.add(internalSplitter);
            renderTimeHandler.setSplitter(internalSplitter);
        }
        else {
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
        initEvents();
        initKeyBindings();

        instance = this;
    }

    private void initConfig() {
        ConfigCodec codec = new YamlCodec();
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("zombiesautosplits");
        String configFileName;
        if (codec.getPreferredExtensions().isEmpty()) {
            configFileName = "config";
        }
        else {
            configFileName = "config." + codec.getPreferredExtension();
        }

        try {
            Files.createDirectories(configPath);
        }
        catch (IOException e) {
            logger.error("Failed to create config directory", e);
            return;
        }
        configPath = configPath.resolve(configFileName);

        ZombiesAutoSplitsConfig defaultConfig =
                new ZombiesAutoSplitsConfig(ZombiesAutoSplitsConfig.DEFAULT_HOST, ZombiesAutoSplitsConfig.DEFAULT_PORT,
                        ZombiesAutoSplitsConfig.DEFAULT_USE_LIVE_SPLITS, ZombiesAutoSplitsConfig.DEFAULT_USE_INTERNAL);
        configHandler.registerLoader(configKey,
                new SyncFileConfigLoader<>(new ZombiesAutoSplitsConfigProcessor(), defaultConfig, configPath, codec));

        ZombiesAutoSplitsConfig loadedConfig;
        try {
            loadedConfig = loadConfigFromFile().get();
        }
        catch (InterruptedException | ExecutionException e) {
            logger.error("Failed to load config", e);
            return;
        }
        setConfig(loadedConfig);
    }

    private void initEvents() {
        CompositeSplitter compositeSplitter = new CompositeSplitter(MinecraftClient.getInstance(), logger, splitters);

        AutoSplitSoundInterceptor soundInterceptor =
                new AutoSplitSoundInterceptor(MinecraftClient.getInstance(), compositeSplitter);
        ClientTickEvents.END_CLIENT_TICK.register(new KeyInputHandler(autoSplitsKeybind, compositeSplitter));
        HudRenderCallback.EVENT.register(renderTimeHandler);
        ClientSoundCallback.EVENT.register(soundInterceptor);

        PacketSerializer packetSerializer =
                PacketSerializers.clientToServerSerializer(() -> new PacketByteBufDataWriter(PacketByteBufs.create()),
                        data -> new PacketByteBufDataReader(new PacketByteBuf(Unpooled.wrappedBuffer(data))));
        PacketHandler<PacketSender> clientToServerHandler =
                new PhantazmMessagingHandler(packetSerializer, PhantazmPacket.TYPE.getId(), compositeSplitter);
        ClientPlayNetworking.registerGlobalReceiver(PhantazmPacket.TYPE, (packet, player, responseSender) -> {
            clientToServerHandler.handleData(responseSender, packet.data());
        });
    }

    private void initKeyBindings() {
        KeyBindingHelper.registerKeyBinding(autoSplitsKeybind);
    }

}
