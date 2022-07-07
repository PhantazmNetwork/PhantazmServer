package com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.directional;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class BetweenPointsFinder implements DirectionalEntityFinder {

    public record Data() implements Keyed {

        public static final Key SERIAL_KEY
                = Key.key(Namespaces.PHANTAZM, "gun.entity_finder.directional.between_points");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor() {
        return ConfigProcessor.emptyProcessor(Data::new);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull Collection<LivingEntity> findEntities(@NotNull Instance instance, @NotNull Pos start,
                                                          @NotNull Point end) {
        Collection<LivingEntity> entities = new ArrayList<>(instance.getEntities().size());
        instance.getEntityTracker().raytraceCandidates(start, end, EntityTracker.Target.LIVING_ENTITIES, entities::add);

        return entities;
    }

}
