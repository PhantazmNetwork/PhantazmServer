package org.phantazm.mob2;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.Scheduler;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.loader.Loader;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class BasicMobSpawner implements MobSpawner {
    public static final ExtensionHolder.Key<MobSpawner> SPAWNER_KEY = MobSpawner.Extensions.newKey(MobSpawner.class);
    public static final ExtensionHolder.Key<Scheduler> SCHEDULER_KEY = MobSpawner.Extensions.newKey(Scheduler.class);

    private final Map<Key, MobCreator> mobCreatorMap;

    public BasicMobSpawner(@NotNull Loader<MobCreator> mobCreatorLoader) {
        this.mobCreatorMap = Objects.requireNonNull(mobCreatorLoader).data();
    }

    @Override
    public @NotNull Mob spawn(@NotNull Key identifier, @NotNull Instance instance, @NotNull Pos pos,
        @NotNull Consumer<? super @NotNull Mob> setup) {
        MobCreator creator = mobCreatorMap.get(identifier);
        if (creator == null) {
            throw new IllegalArgumentException("missing mob identifier " + identifier);
        }

        ExtensionHolder mobHolder = creator.typeExtensions().derive();
        buildDependencies(mobHolder);

        Mob mob = creator.create(instance, mobHolder);
        setup.accept(mob);

        preSetup(mob);
        mob.setInstance(instance, pos).whenComplete((ignored, error) -> {
            if (error != null) {
                return;
            }

            postSetup(mob);
        });

        return mob;
    }

    @Override
    public boolean canSpawn(@NotNull Key identifier) {
        return mobCreatorMap.containsKey(identifier);
    }

    protected void buildDependencies(@NotNull ExtensionHolder extensionHolder) {
        extensionHolder.set(SPAWNER_KEY, this);
    }

    /**
     * Called directly before the mob is added to an instance. Does nothing by default, but can be overridden by
     * subclasses. Since the mob is not being ticked yet, it is not necessary to acquire it before calling any of its
     * methods.
     *
     * @param mob the mob to set up
     */
    public void preSetup(@NotNull Mob mob) {
        //no-op
    }

    /**
     * Called directly after the mob is added to an instance. Can be used for applying global settings. Does nothing by
     * default, but can be overridden by subclasses if desired. Since the mob can be ticked, it is necessary to acquire
     * it before calling many of its methods.
     *
     * @param mob the mob to set up
     */
    public void postSetup(@NotNull Mob mob) {
        //no-op
    }
}
