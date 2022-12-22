package org.phantazm.zombies.equipment.gun.shoot.handler;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import net.minestom.server.potion.Potion;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.shoot.GunHit;
import org.phantazm.zombies.equipment.gun.shoot.GunShot;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * A {@link ShotHandler} that applies a {@link Potion} to {@link Entity} targets.
 */
@Model("zombies.gun.shot_handler.potion")
@Cache
public class PotionShotHandler implements ShotHandler {

    private final Data data;

    /**
     * Creates a {@link PotionShotHandler}.
     *
     * @param data The {@link PotionShotHandler}'s {@link Data}
     */
    @FactoryMethod
    public PotionShotHandler(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public void handle(@NotNull GunState state, @NotNull Entity attacker, @NotNull Collection<UUID> previousHits,
            @NotNull GunShot shot) {
        for (GunHit target : shot.regularTargets()) {
            target.entity().addEffect(data.potion());
        }
        for (GunHit target : shot.headshotTargets()) {
            target.entity().addEffect(data.headshotPotion());
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    /**
     * Data for a {@link PotionShotHandler}.
     *
     * @param potion         The {@link Potion} to apply to regular {@link Entity} targets
     * @param headshotPotion The {@link Potion} to apply to headshot {@link Entity} targets
     */
    @DataObject
    public record Data(@NotNull Potion potion, @NotNull Potion headshotPotion) {

        public Data {
            Objects.requireNonNull(potion, "potion");
            Objects.requireNonNull(headshotPotion, "headshotPotion");
        }

    }

}
