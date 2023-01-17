package org.phantazm.zombies.powerup.action;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.MobStore;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicate;
import org.phantazm.zombies.powerup.predicate.ImmediateDeactivationPredicate;
import org.phantazm.zombies.powerup.Powerup;

import java.util.Objects;
import java.util.function.Supplier;

@Model("zombies.powerup.action.kill_all_in_radius")
public class KillAllInRadiusAction implements Supplier<PowerupAction> {
    private final Data data;
    private final Instance instance;
    private final MobStore mobStore;

    @FactoryMethod
    public KillAllInRadiusAction(@NotNull Data data, @NotNull Instance instance, @NotNull MobStore mobStore) {
        this.data = Objects.requireNonNull(data, "data");
        this.instance = Objects.requireNonNull(instance, "instance");
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
    }

    @Override
    public PowerupAction get() {
        return new Action(data, instance, mobStore);
    }

    @DataObject
    public record Data(double radius) {

    }

    private static class Action extends InstantAction {
        private final Data data;
        private final Instance instance;
        private final MobStore mobStore;

        private Action(Data data, Instance instance, MobStore mobStore) {
            this.instance = instance;
            this.data = data;
            this.mobStore = mobStore;
        }

        @Override
        public void activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer player, long time) {
            instance.getEntityTracker()
                    .nearbyEntities(powerup.spawnLocation(), data.radius, EntityTracker.Target.LIVING_ENTITIES,
                            entity -> {
                                if (mobStore.hasMob(entity.getUuid())) {
                                    entity.kill();
                                }
                            });
        }
    }
}
