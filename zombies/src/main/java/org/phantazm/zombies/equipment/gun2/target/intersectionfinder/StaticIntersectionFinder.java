package org.phantazm.zombies.equipment.gun2.target.intersectionfinder;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.PlayerComponent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Optional;

public class StaticIntersectionFinder implements PlayerComponent<IntersectionFinder> {

    private static final IntersectionFinder FINDER = (entity, start, end, distanceLimitSquared) -> {

        BoundingBox boundingBox = entity.getBoundingBox();
        Pos position = entity.getPosition();

        double halfWidth = boundingBox.width() / 2;
        double halfHeight = boundingBox.height() / 2;
        return Optional.of(new Vec(position.x() + halfWidth, position.y() + halfHeight, position.z() + halfWidth));
    };

    @Override
    public @NotNull IntersectionFinder forPlayer(@NotNull ZombiesPlayer player, @NotNull InjectionStore injectionStore) {
        return FINDER;
    }
}
