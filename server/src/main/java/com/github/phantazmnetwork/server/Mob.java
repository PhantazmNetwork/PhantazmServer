package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.core.config.processor.ItemStackConfigProcessors;
import com.github.phantazmnetwork.core.config.processor.VariantConfigProcessor;
import com.github.phantazmnetwork.mob.MobModel;
import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.mob.config.MobModelConfigProcessor;
import com.github.phantazmnetwork.mob.goal.FollowPlayerGoal;
import com.github.phantazmnetwork.mob.goal.UseSkillGoal;
import com.github.phantazmnetwork.mob.skill.PlaySoundSkill;
import com.github.phantazmnetwork.mob.spawner.BasicMobSpawner;
import com.github.phantazmnetwork.mob.spawner.MobSpawner;
import com.github.phantazmnetwork.mob.target.EntitySelector;
import com.github.phantazmnetwork.mob.target.NearestPlayerSelector;
import com.github.phantazmnetwork.mob.trigger.MobTrigger;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.GroundMinestomDescriptor;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.MinestomDescriptor;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.Spawner;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.config.GroundMinestomDescriptorConfigProcessor;
import com.github.phantazmnetwork.neuron.node.Calculator;
import com.github.phantazmnetwork.neuron.node.config.CalculatorConfigProcessor;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.core.bridge.ConfigBridges;
import com.github.steanky.ethylene.core.codec.ConfigCodec;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Main entrypoint for PhantazmMob related features.
 */
public final class Mob {

    private static final Logger LOGGER = LoggerFactory.getLogger(Mob.class);
    private static final ConfigProcessor<MobModel> MODEL_PROCESSOR;
    private static final MobStore MOB_STORE = new MobStore();
    private static MobSpawner mobSpawner;
    private static Map<Key, MobModel> models;

    static {
        ConfigProcessor<Calculator> calculatorProcessor = new CalculatorConfigProcessor();
        ConfigProcessor<MinestomDescriptor> descriptorProcessor = new VariantConfigProcessor<>(
                Map.of(GroundMinestomDescriptor.SERIAL_KEY,
                        new GroundMinestomDescriptorConfigProcessor(calculatorProcessor))::get);

        MODEL_PROCESSOR = new MobModelConfigProcessor(descriptorProcessor, ItemStackConfigProcessors.snbt());
    }

    private Mob() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("SameParameterValue")
    static void initialize(@NotNull EventNode<Event> global, @NotNull ContextManager contextManager,
            @NotNull KeyParser keyParser, @NotNull Spawner spawner, @NotNull Collection<MobTrigger<?>> triggers,
            @NotNull Path mobPath, @NotNull ConfigCodec codec) {
        registerElementClasses(contextManager);

        mobSpawner = new BasicMobSpawner(MOB_STORE, spawner, contextManager, keyParser);

        global.addListener(EntityDeathEvent.class, MOB_STORE::onMobDeath);
        for (MobTrigger<?> trigger : triggers) {
            registerTrigger(global, MOB_STORE, trigger);
        }

        loadModels(mobPath, codec);
    }

    private static void registerElementClasses(@NotNull ContextManager contextManager) {
        //mob goals
        contextManager.registerElementClass(FollowPlayerGoal.class);
        contextManager.registerElementClass(UseSkillGoal.class);

        //mob skills
        contextManager.registerElementClass(PlaySoundSkill.class);

        //mob selectors
        contextManager.registerElementClass(EntitySelector.class);
        contextManager.registerElementClass(NearestPlayerSelector.class);
    }

    @SuppressWarnings("SameParameterValue")
    private static <T extends Event> void registerTrigger(@NotNull EventNode<? super T> node,
            @NotNull MobStore mobStore, @NotNull MobTrigger<T> trigger) {
        node.addListener(trigger.eventClass(),
                event -> mobStore.useTrigger(trigger.entityGetter().apply(event), trigger.key()));
    }

    private static void loadModels(@NotNull Path mobPath, @NotNull ConfigCodec codec) {
        LOGGER.info("Loading mob files...");

        Map<Key, MobModel> loadedModels = new HashMap<>();
        try {
            Files.createDirectories(mobPath);

            try (Stream<Path> paths = Files.list(mobPath)) {
                String ending =
                        codec.getPreferredExtensions().isEmpty() ? "" : "." + codec.getPreferredExtensions().get(0);
                PathMatcher matcher = mobPath.getFileSystem().getPathMatcher("glob:**." + ending);
                paths.forEach(path -> {
                    if (matcher.matches(path) && Files.isRegularFile(path)) {
                        try {
                            MobModel model = ConfigBridges.read(path, codec, getModelProcessor());
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
     * Gets the {@link MobStore} used by the {@link Mob} system.
     *
     * @return The {@link MobStore} used by the {@link Mob} system
     */
    public static MobStore getMobStore() {
        return MOB_STORE;
    }

    /**
     * Gets the global {@link MobSpawner}.
     *
     * @return The global {@link MobSpawner}.
     */
    public static @NotNull MobSpawner getMobSpawner() {
        return requireInitialized(mobSpawner);
    }

    /**
     * Gets the loaded {@link MobModel}s.
     *
     * @return The loaded {@link MobModel}s
     */
    @SuppressWarnings("unused")
    public static @NotNull @Unmodifiable Map<Key, MobModel> getModels() {
        return requireInitialized(models);
    }

    private static <TObject> @NotNull TObject requireInitialized(TObject object) {
        if (object == null) {
            throw new IllegalStateException("PhantazmMob has not been initialized yet");
        }

        return object;
    }

}
