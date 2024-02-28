package org.phantazm.zombies.mob2;

import net.minestom.server.network.packet.server.play.TeamsPacket;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.loader.Loader;
import org.phantazm.mob2.BasicMobSpawner;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.MobCreator;
import org.phantazm.mob2.MobSpawner;
import org.phantazm.zombies.Stages;
import org.phantazm.zombies.event.mob.ZombiesMobSetupEvent;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Objects;
import java.util.function.Supplier;

public class ZombiesMobSpawner extends BasicMobSpawner {
    public static ExtensionHolder.Key<ZombiesScene> SCENE_KEY = MobSpawner.Extensions.newKey(ZombiesScene.class);

    private final Supplier<ZombiesScene> scene;

    public ZombiesMobSpawner(@NotNull Loader<MobCreator> mobCreatorLoader, @NotNull Supplier<ZombiesScene> scene) {
        super(mobCreatorLoader);
        this.scene = Objects.requireNonNull(scene);
    }

    @Override
    public void buildDependencies(@NotNull ExtensionHolder holder) {
        super.buildDependencies(holder);
        ZombiesScene scene = this.scene.get();

        holder.set(SCENE_KEY, scene);
        holder.set(SCHEDULER_KEY, scene.getScheduler());
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