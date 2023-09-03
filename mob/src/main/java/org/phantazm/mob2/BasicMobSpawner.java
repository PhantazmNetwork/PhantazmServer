package org.phantazm.mob2;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;

import java.util.Map;
import java.util.function.Consumer;

import org.phantazm.commons.InjectionStore.Builder;

public class BasicMobSpawner implements MobSpawner {
    private final Map<Key, MobCreator> mobCreators;
    private final InjectionStore.Builder builder;

    private volatile InjectionStore injectionStore;

    public BasicMobSpawner(@NotNull Map<Key, MobCreator> mobCreators) {
        this.mobCreators = Map.copyOf(mobCreators);
        this.builder = InjectionStore.builder();
    }

    @Override
    public @NotNull Mob spawn(@NotNull Key identifier, @NotNull Instance instance, @NotNull Pos pos,
        @NotNull Consumer<? super @NotNull Mob> setup) {
        InjectionStore store = this.injectionStore;
        if (store == null) {
            throw new IllegalStateException("this spawner has not yet been initialized");
        }

        MobCreator creator = mobCreators.get(identifier);
        if (creator == null) {
            throw new IllegalArgumentException("missing mob identifier " + identifier);
        }

        Mob mob = creator.create(instance, store);
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
        return mobCreators.containsKey(identifier);
    }

    @Override
    public void init() {
        builder.clear();
        buildDependencies(builder);
        this.injectionStore = builder.build();
    }

    /**
     * Builds dependencies. By default, the only dependency added is this MobSpawner, under the key
     * {@link Keys#MOB_SPAWNER}. This method can be overridden by subclasses to add additional dependencies, which can
     * be appended to the builder using {@link Builder#with(InjectionStore.Key, Object)}.
     *
     * @param builder the builder to which additional dependencies may be added
     */
    protected void buildDependencies(InjectionStore.@NotNull Builder builder) {
        builder.with(Keys.MOB_SPAWNER, this);
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
