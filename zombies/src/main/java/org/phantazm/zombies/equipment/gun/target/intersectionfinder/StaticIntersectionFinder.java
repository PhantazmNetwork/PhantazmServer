package org.phantazm.zombies.equipment.gun.target.intersectionfinder;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * An {@link IntersectionFinder} that always finds an intersection at the center of an {@link Entity}.
 */
@Model("zombies.gun.intersection_finder.static")
@Cache
public class StaticIntersectionFinder implements IntersectionFinder {

    @FactoryMethod
    public StaticIntersectionFinder() {

    }

    @Override
    public @NotNull Optional<Vec> getHitLocation(@NotNull Entity entity, @NotNull Pos start) {
        BoundingBox boundingBox = entity.getBoundingBox();
        Pos position = entity.getPosition();

        double halfWidth = boundingBox.width() / 2;
        double halfHeight = boundingBox.height() / 2;
        return Optional.of(new Vec(position.x() + halfWidth, position.y() + halfHeight, position.z() + halfWidth));
    }

}
