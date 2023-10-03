package org.phantazm.zombies.mob2;

import net.kyori.adventure.key.Key;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.BasicMobSpawner;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.MobCreator;
import org.phantazm.zombies.Stages;
import org.phantazm.zombies.event.ZombiesMobSetupEvent;
import org.phantazm.zombies.scene2.ZombiesScene;

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
        builder.with(Keys.SCENE, scene.get());
    }

    @Override
    public void preSetup(@NotNull Mob mob) {
        super.preSetup(mob);

        if (mob.useStateHolder()) {
            mob.stateHolder().setStage(Stages.ZOMBIES_GAME);
        }

        ZombiesScene scene = this.scene.get();
        boolean mobPlayerCollisions = scene.mapSettingsInfo().mobPlayerCollisions();
        mob.setCollisionRule(mobPlayerCollisions ? TeamsPacket.CollisionRule.PUSH_OTHER_TEAMS :
            TeamsPacket.CollisionRule.NEVER);

        scene.broadcastEvent(new ZombiesMobSetupEvent(mob));
    }
}