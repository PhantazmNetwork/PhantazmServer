package org.phantazm.zombies.equipment.gun2.target.headshot;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.PlayerComponent;
import org.phantazm.zombies.player.ZombiesPlayer;

public class EyeHeightHeadshotTester implements PlayerComponent<HeadshotTester> {

    private static final HeadshotTester TESTER = (shooter, entity, intersection) -> {
        double eyeHeight = entity.getPosition().y() + entity.getEyeHeight();
        return intersection.y() >= eyeHeight ||
            (eyeHeight - intersection.y()) <= (entity.getBoundingBox().height()) - entity.getEyeHeight();
    };

    @Override
    public @NotNull HeadshotTester forPlayer(@NotNull ZombiesPlayer player, @NotNull InjectionStore injectionStore) {
        return TESTER;
    }
}
