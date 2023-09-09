package org.phantazm.zombies.equipment.gun2.target.entityfinder.directional;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.PlayerComponent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.ArrayList;
import java.util.Collection;

public class BetweenPointsFinder implements PlayerComponent<DirectionalEntityFinder> {

    private static final DirectionalEntityFinder FINDER = new DirectionalEntityFinder() {
        @Override
        public @NotNull Collection<LivingEntity> findEntities(@NotNull Instance instance, @NotNull Pos start,
            @NotNull Point end) {
            Collection<LivingEntity> entities = new ArrayList<>();
            instance.getEntityTracker().raytraceCandidates(start, end, EntityTracker.Target.LIVING_ENTITIES, entities::add);

            return entities;
        }
    };

    @Override
    public @NotNull DirectionalEntityFinder forPlayer(@NotNull ZombiesPlayer player, @NotNull InjectionStore injectionStore) {
        return FINDER;
    }
}
