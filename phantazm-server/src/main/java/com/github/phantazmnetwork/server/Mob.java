package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.api.config.ItemStackConfigProcessors;
import com.github.phantazmnetwork.api.config.VariantConfigProcessor;
import com.github.phantazmnetwork.mob.MobModel;
import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.mob.config.MobModelConfigProcessor;
import com.github.phantazmnetwork.mob.config.goal.FollowEntityGoalConfigProcessor;
import com.github.phantazmnetwork.mob.config.goal.UseSkillGoalConfigProcessor;
import com.github.phantazmnetwork.mob.config.skill.PlaySoundSkillConfigProcessor;
import com.github.phantazmnetwork.mob.config.target.MappedSelectorConfigProcessor;
import com.github.phantazmnetwork.mob.config.target.NearestEntitiesSelectorConfigProcessor;
import com.github.phantazmnetwork.mob.goal.FollowEntityGoal;
import com.github.phantazmnetwork.mob.goal.FollowPlayerGoal;
import com.github.phantazmnetwork.mob.goal.Goal;
import com.github.phantazmnetwork.mob.goal.UseSkillGoal;
import com.github.phantazmnetwork.mob.skill.PlaySoundSkill;
import com.github.phantazmnetwork.mob.skill.Skill;
import com.github.phantazmnetwork.mob.spawner.BasicMobSpawner;
import com.github.phantazmnetwork.mob.spawner.MobSpawner;
import com.github.phantazmnetwork.mob.target.FirstTargetSelector;
import com.github.phantazmnetwork.mob.target.NearestEntitiesSelector;
import com.github.phantazmnetwork.mob.target.NearestPlayersSelector;
import com.github.phantazmnetwork.mob.target.TargetSelector;
import com.github.phantazmnetwork.mob.trigger.MobTrigger;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.GroundMinestomDescriptor;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.MinestomDescriptor;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.Spawner;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.config.GroundMinestomDescriptorConfigProcessor;
import com.github.phantazmnetwork.neuron.node.Calculator;
import com.github.phantazmnetwork.neuron.node.config.CalculatorConfigProcessor;
import com.github.steanky.ethylene.core.bridge.ConfigBridges;
import com.github.steanky.ethylene.core.codec.ConfigCodec;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
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

    private Mob() {
        throw new UnsupportedOperationException();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Mob.class);

    private static final ConfigProcessor<MobModel> MODEL_PROCESSOR;

    private static final MobStore MOB_STORE = new MobStore();

    private static MobSpawner mobSpawner;

    private static Map<Key, MobModel> models;

    static {
        ConfigProcessor<Calculator> calculatorProcessor = new CalculatorConfigProcessor();
        ConfigProcessor<MinestomDescriptor> descriptorProcessor = new VariantConfigProcessor<>(Map.of(
                GroundMinestomDescriptor.SERIAL_KEY, new GroundMinestomDescriptorConfigProcessor(calculatorProcessor)
        )::get);
        ConfigProcessor<NearestEntitiesSelector<Player>> nearestPlayersSelectorProcessor = new NearestEntitiesSelectorConfigProcessor<NearestEntitiesSelector<Player>>() {
            @Override
            protected @NotNull NearestEntitiesSelector<Player> createSelector(double range, int targetLimit) {
                return new NearestPlayersSelector(range, targetLimit);
            }
        };
        ConfigProcessor<? extends TargetSelector<Player>> nearestPlayerSelector = new MappedSelectorConfigProcessor<Iterable<Player>, FirstTargetSelector<Player>>(nearestPlayersSelectorProcessor) {
            @Override
            protected @NotNull FirstTargetSelector<Player> createSelector(@NotNull TargetSelector<Iterable<Player>> delegate) {
                return new FirstTargetSelector<>(delegate);
            }
        };
        ConfigProcessor<TargetSelector<? extends Audience>> audienceSelectorProcessor = new VariantConfigProcessor<>(Map.of(
                NearestPlayersSelector.SERIAL_KEY, nearestPlayerSelector
        )::get);
        ConfigProcessor<Skill> skillProcessor = new VariantConfigProcessor<>(Map.of(
                PlaySoundSkill.SERIAL_KEY, new PlaySoundSkillConfigProcessor(audienceSelectorProcessor)
        )::get);
        ConfigProcessor<TargetSelector<Player>> playerSelectorProcessor = new VariantConfigProcessor<>(Map.of(
                NearestPlayersSelector.SERIAL_KEY, nearestPlayerSelector
        )::get);
        ConfigProcessor<FollowEntityGoal<Player>> followPlayerGoalProcessor = new FollowEntityGoalConfigProcessor<>(playerSelectorProcessor) {
            @Override
            protected @NotNull FollowEntityGoal<Player> createGoal(@NotNull TargetSelector<Player> selector) {
                return new FollowEntityGoal<>(selector) {
                    @Override
                    public @NotNull Key getSerialKey() {
                        return FollowPlayerGoal.SERIAL_KEY;
                    }
                };
            }
        };
        ConfigProcessor<Goal> goalProcessor = new VariantConfigProcessor<>(Map.of(
                UseSkillGoal.SERIAL_KEY, new UseSkillGoalConfigProcessor(skillProcessor),
                FollowPlayerGoal.SERIAL_KEY, followPlayerGoalProcessor
        )::get);
        MODEL_PROCESSOR = new MobModelConfigProcessor(
                descriptorProcessor,
                goalProcessor,
                skillProcessor,
                ItemStackConfigProcessors.snbt()
        );
    }

    @SuppressWarnings("SameParameterValue")
    static void initialize(@NotNull EventNode<Event> global, @NotNull Spawner spawner,
                           @NotNull Collection<MobTrigger<?>> triggers, @NotNull Path mobPath,
                           @NotNull ConfigCodec codec) {
        mobSpawner = new BasicMobSpawner(MOB_STORE, spawner);

        global.addListener(EntityDeathEvent.class, MOB_STORE::onMobDeath);
        for (MobTrigger<?> trigger : triggers) {
            registerTrigger(global, MOB_STORE, trigger);
        }

        loadModels(mobPath, codec);
    }

    private static <T extends Event> void registerTrigger(@NotNull EventNode<? super T> node, @NotNull MobStore mobStore, @NotNull MobTrigger<T> trigger) {
        node.addListener(trigger.eventClass(), event -> {
            mobStore.useTrigger(trigger.entityGetter().apply(event), trigger.key());
        });
    }

    private static void loadModels(@NotNull Path mobPath, @NotNull ConfigCodec codec) {
        LOGGER.info("Loading mob files...");

        Map<Key, MobModel> loadedModels = new HashMap<>();
        try (Stream<Path> paths = Files.list(mobPath)) {
            PathMatcher matcher = mobPath.getFileSystem().getPathMatcher("glob:**." + codec.getPreferredExtension());
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
                    } catch (IOException e) {
                        LOGGER.warn("Could not load mob file", e);
                    }
                }
            });
        } catch (IOException e) {
            LOGGER.warn("Could not list files in mob directory", e);
        }
        models = Map.copyOf(loadedModels);

        LOGGER.info("Loaded {} mob files.", models.size());
    }

    /**
     * Gets a {@link ConfigProcessor} for {@link MobModel}s.
     * @return A {@link ConfigProcessor} for {@link MobModel}s.
     */
    public static @NotNull ConfigProcessor<MobModel> getModelProcessor() {
        return MODEL_PROCESSOR;
    }

    public static MobStore getMobStore() {
        return MOB_STORE;
    }

    /**
     * Gets the global {@link MobSpawner}.
     * @return The global {@link MobSpawner}.
     */
    public static @NotNull MobSpawner getMobSpawner() {
        return requireInitialized(mobSpawner);
    }

    /**
     * Gets the loaded {@link MobModel}s.
     * @return The loaded {@link MobModel}s
     */
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
