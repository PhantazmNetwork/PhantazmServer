package org.phantazm.zombies.equipment.gun.shoot.handler;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.shoot.GunHit;
import org.phantazm.zombies.equipment.gun.shoot.GunShot;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * A {@link ShotHandler} that sets {@link Entity}s on fire.
 */
@Model("zombies.gun.shot_handler.ignite")
@Cache
public class IgniteShotHandler implements ShotHandler {

    private final Data data;

    /**
     * Creates an {@link IgniteShotHandler}.
     *
     * @param data The {@link IgniteShotHandler}'s {@link Data}
     */
    @FactoryMethod
    public IgniteShotHandler(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public void handle(@NotNull Gun gun, @NotNull GunState state, @NotNull Entity attacker,
            @NotNull Collection<UUID> previousHits, @NotNull GunShot shot) {
        for (GunHit target : shot.regularTargets()) {
            target.entity().setFireForDuration(data.duration());
        }
        for (GunHit target : shot.headshotTargets()) {
            target.entity().setFireForDuration(data.headshotDuration());
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    /**
     * Data for an {@link IgniteShotHandler}.
     *
     * @param duration         The duration of the fire for regular targets
     * @param headshotDuration The duration of the fire for headshots
     */
    @DataObject
    public record Data(int duration, int headshotDuration) {

    }

}
