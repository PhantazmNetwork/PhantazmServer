package org.phantazm.server;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.bridge.Configuration;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import it.unimi.dsi.fastutil.booleans.BooleanObjectPair;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.villager.VillagerMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.phantazm.commons.ConfigProcessors;
import org.phantazm.commons.FileUtils;
import org.phantazm.core.config.processor.ItemStackConfigProcessors;
import org.phantazm.core.config.processor.MinestomConfigProcessors;
import org.phantazm.mob.MobModel;
import org.phantazm.mob.config.MobModelConfigProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Main entrypoint for PhantazmMob related features.
 */
public final class MobFeature {
    private static final Logger LOGGER = LoggerFactory.getLogger(MobFeature.class);

    private static ConfigProcessor<MobModel> MODEL_PROCESSOR;
    private static Map<Key, MobModel> models;
    private static Map<BooleanObjectPair<String>, ConfigProcessor<?>> processorMap;

    private MobFeature() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("SameParameterValue")
    static void initialize(@NotNull ContextManager contextManager, @NotNull Path mobPath, @NotNull ConfigCodec codec) {
        MODEL_PROCESSOR = new MobModelConfigProcessor(contextManager, ItemStackConfigProcessors.snbt());

        Map<BooleanObjectPair<String>, ConfigProcessor<?>> processorMap = new HashMap<>();
        processorMap.put(BooleanObjectPair.of(false, byte.class.getName()), ConfigProcessor.BYTE);
        processorMap.put(BooleanObjectPair.of(false, int.class.getName()), ConfigProcessor.INTEGER);
        processorMap.put(BooleanObjectPair.of(false, float.class.getName()), ConfigProcessor.FLOAT);
        processorMap.put(BooleanObjectPair.of(false, String.class.getName()), ConfigProcessor.STRING);
        processorMap.put(BooleanObjectPair.of(false, Component.class.getName()), ConfigProcessors.component());
        processorMap.put(BooleanObjectPair.of(true, Component.class.getName()),
                ConfigProcessors.component().optionalProcessor());
        processorMap.put(BooleanObjectPair.of(false, ItemStack.class.getName()), ItemStackConfigProcessors.snbt());
        processorMap.put(BooleanObjectPair.of(false, boolean.class.getName()), ConfigProcessor.BOOLEAN);
        processorMap.put(BooleanObjectPair.of(false, Point.class.getName()), MinestomConfigProcessors.point());
        processorMap.put(BooleanObjectPair.of(true, Point.class.getName()),
                MinestomConfigProcessors.point().optionalProcessor());
        processorMap.put(BooleanObjectPair.of(false, Direction.class.getName()),
                ConfigProcessor.enumProcessor(Direction.class));
        processorMap.put(BooleanObjectPair.of(true, UUID.class.getName()), ConfigProcessors.uuid());
        processorMap.put(BooleanObjectPair.of(true, Integer.class.getName()), new ConfigProcessor<Integer>() {

            private final ConfigProcessor<Integer> delegate = ConfigProcessor.INTEGER;

            @Override
            public @Nullable Integer dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                if (element.isNull()) {
                    return null;
                }

                return delegate.dataFromElement(element);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@Nullable Integer integer) throws ConfigProcessException {
                if (integer == null) {
                    return ConfigPrimitive.NULL;
                }

                return delegate.elementFromData(integer);
            }
        });
        processorMap.put(BooleanObjectPair.of(false, NBT.class.getName()), MinestomConfigProcessors.nbt());
        processorMap.put(BooleanObjectPair.of(false, VillagerMeta.VillagerData.class.getName()),
                MinestomConfigProcessors.villagerData());
        processorMap.put(BooleanObjectPair.of(false, Entity.Pose.class.getName()),
                ConfigProcessor.enumProcessor(Entity.Pose.class));

        MobFeature.processorMap = Map.copyOf(processorMap);

        loadModels(mobPath, codec);
    }

    private static void loadModels(@NotNull Path mobPath, @NotNull ConfigCodec codec) {
        Map<Key, MobModel> loadedModels = new HashMap<>();
        try {
            FileUtils.createDirectories(mobPath);

            try (Stream<Path> paths = Files.walk(mobPath)) {
                String ending = codec.getPreferredExtension();
                PathMatcher matcher = mobPath.getFileSystem().getPathMatcher("glob:**" + ending);
                paths.forEach(path -> {
                    if (matcher.matches(path) && Files.isRegularFile(path)) {
                        try {
                            MobModel model = Configuration.read(path, codec, getModelProcessor());
                            if (loadedModels.containsKey(model.key())) {
                                LOGGER.warn("Duplicate key ({}), skipping...", model.key());
                            }
                            else {
                                loadedModels.put(model.key(), model);
                            }
                        }
                        catch (IOException e) {
                            LOGGER.warn("Could not load mob file", e);
                        }
                    }
                });
            }
            catch (IOException e) {
                LOGGER.warn("Could not list files in mob directory", e);
            }
        }
        catch (IOException e) {
            LOGGER.warn("Failed to create directory {}", mobPath);
        }
        models = Map.copyOf(loadedModels);
        LOGGER.info("Loaded {} mob files.", models.size());
    }

    /**
     * Gets a {@link ConfigProcessor} for {@link MobModel}s.
     *
     * @return A {@link ConfigProcessor} for {@link MobModel}s.
     */
    public static @NotNull ConfigProcessor<MobModel> getModelProcessor() {
        return MODEL_PROCESSOR;
    }

    /**
     * Gets the loaded {@link MobModel}s.
     *
     * @return The loaded {@link MobModel}s
     */
    @SuppressWarnings("unused")
    public static @NotNull @Unmodifiable Map<Key, MobModel> getModels() {
        return FeatureUtils.check(models);
    }

    public static @NotNull Map<BooleanObjectPair<String>, ConfigProcessor<?>> getProcessorMap() {
        return FeatureUtils.check(processorMap);
    }
}
