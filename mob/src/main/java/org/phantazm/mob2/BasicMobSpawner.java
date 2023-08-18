package org.phantazm.mob2;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;

import java.util.Map;
import java.util.function.Consumer;

public class BasicMobSpawner implements MobSpawner {
    private final Map<Key, MobCreator> mobCreators;
    private final InjectionStore injectionStore;

    public BasicMobSpawner(@NotNull Map<Key, MobCreator> mobCreators) {
        this.mobCreators = Map.copyOf(mobCreators);
        this.injectionStore = InjectionStore.of(Keys.MOB_SPAWNER, this);
    }

    @Override
    public @NotNull Mob spawn(@NotNull Key identifier, @NotNull Instance instance, @NotNull Pos pos,
            @NotNull Consumer<? super Mob> setup) {
        MobCreator creator = mobCreators.get(identifier);
        if (creator == null) {
            throw new IllegalArgumentException("missing mob identifier " + identifier);
        }

        Mob mob = creator.create(instance, injectionStore);
        setup.accept(mob);

        mob.setInstance(instance, pos);
        return mob;
    }
}
