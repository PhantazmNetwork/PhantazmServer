package org.phantazm.zombies.mob2;

import net.kyori.adventure.key.Key;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.BasicMobSpawner;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.MobCreator;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class ZombiesMobSpawner extends BasicMobSpawner {
    private final Supplier<ZombiesScene> scene;

    public ZombiesMobSpawner(@NotNull Map<Key, MobCreator> mobCreators, @NotNull Supplier<ZombiesScene> scene) {
        super(mobCreators);
        this.scene = Objects.requireNonNull(scene);
    }

    @Override
    public void buildDependencies(InjectionStore.@NotNull Builder builder) {
        super.buildDependencies(builder);
        builder.with(Keys.SCENE, Objects.requireNonNull(scene.get()));
    }

    @Override
    public void postSetup(@NotNull Mob mob) {
        super.postSetup(mob);

        ZombiesScene scene = this.scene.get();
        boolean mobPlayerCollisions = scene.getMapSettingsInfo().mobPlayerCollisions();
        if (mobPlayerCollisions) {
            mob.getAcquirable().sync(self -> {
                ((Mob) self).setCollisionRule(TeamsPacket.CollisionRule.PUSH_OTHER_TEAMS);
            });
        }
    }
}
