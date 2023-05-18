package org.phantazm.zombies.equipment.gun.target.headshot;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link HeadshotTester} based on the eye height of the target.
 */
@Model("zombies.gun.headshot_tester.eye_height")
@Cache
public class EyeHeightHeadshotTester implements HeadshotTester {

    @FactoryMethod
    public EyeHeightHeadshotTester() {

    }

    @Override
    public boolean isHeadshot(@NotNull Entity shooter, @NotNull Entity entity, @NotNull Point intersection) {
        double eyeHeight = entity.getPosition().y() + entity.getEyeHeight();
        return intersection.y() >= eyeHeight ||
                (eyeHeight - intersection.y()) <= (entity.getBoundingBox().height()) - entity.getEyeHeight();
    }
}
