package org.phantazm.zombies.equipment.gun2.target.entityfinder.directional;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.PlayerComponent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class AroundEndFinder implements PlayerComponent<DirectionalEntityFinder> {

    private final Data data;

    public AroundEndFinder(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull DirectionalEntityFinder forPlayer(@NotNull ZombiesPlayer player, @NotNull InjectionStore injectionStore) {
        return new Finder(data.range());
    }

    private static class Finder implements DirectionalEntityFinder {

        private final double range;

        public Finder(double range) {
            this.range = range;
        }

        @Override
        public @NotNull Collection<LivingEntity> findEntities(@NotNull Instance instance, @NotNull Pos start, @NotNull Point end) {
            Collection<Entity> entities = instance.getNearbyEntities(end, range);
            Collection<LivingEntity> livingEntities = new ArrayList<>(entities.size());
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntities.add(livingEntity);
                }
            }

            return livingEntities;
        }
    }

    public record Data(double range) {

    }

}
