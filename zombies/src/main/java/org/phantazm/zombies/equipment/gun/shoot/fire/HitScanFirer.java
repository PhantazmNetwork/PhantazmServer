package org.phantazm.zombies.equipment.gun.shoot.fire;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.event.GunShootEvent;
import org.phantazm.zombies.equipment.gun.shoot.GunShot;
import org.phantazm.zombies.equipment.gun.shoot.endpoint.ShotEndpointSelector;
import org.phantazm.zombies.equipment.gun.shoot.handler.ShotHandler;
import org.phantazm.zombies.equipment.gun.target.TargetFinder;
import org.phantazm.zombies.event.equipment.EntitiesHitByGunEvent;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.*;
import java.util.function.Supplier;

/**
 * A {@link Firer} that fires using "hit scan". Targets will be immediately found and shot.
 */
@Model("zombies.gun.firer.hit_scan")
@Cache(false)
public class HitScanFirer implements Firer {
    private final Supplier<Optional<? extends Entity>> entitySupplier;
    private final ZombiesScene zombiesScene;
    private final ShotEndpointSelector endSelector;
    private final TargetFinder targetFinder;
    private final Collection<ShotHandler> shotHandlers;

    /**
     * Creates a {@link HitScanFirer}.
     *
     * @param entitySupplier A {@link Supplier} for the {@link Entity} shooter
     * @param endSelector    The {@link HitScanFirer}'s {@link ShotEndpointSelector}
     * @param targetFinder   The {@link HitScanFirer}'s {@link TargetFinder}
     * @param shotHandlers   The {@link HitScanFirer}'s {@link ShotHandler}s
     */
    @FactoryMethod
    public HitScanFirer(@NotNull Supplier<Optional<? extends Entity>> entitySupplier,
        @NotNull ZombiesScene zombiesScene,
        @NotNull @Child("endSelector") ShotEndpointSelector endSelector,
        @NotNull @Child("targetFinder") TargetFinder targetFinder,
        @NotNull @Child("shotHandlers") Collection<ShotHandler> shotHandlers) {
        this.entitySupplier = Objects.requireNonNull(entitySupplier);
        this.zombiesScene = Objects.requireNonNull(zombiesScene);
        this.endSelector = Objects.requireNonNull(endSelector);
        this.targetFinder = Objects.requireNonNull(targetFinder);
        this.shotHandlers = List.copyOf(shotHandlers);
    }

    @Override
    public void fire(@NotNull Gun gun, @NotNull GunState state, @NotNull Pos start,
        @NotNull Collection<UUID> previousHits) {
        entitySupplier.get().ifPresent(entity -> {
            Optional<Point> endOptional = endSelector.getEnd(start);
            if (endOptional.isEmpty()) {
                return;
            }

            Point end = endOptional.get();
            TargetFinder.Result target = targetFinder.findTarget(gun, entity, start, end, previousHits);

            GunShot shot = new GunShot(start, end, target.hits());
            zombiesScene.broadcastEvent(new GunShootEvent(gun, shot, entity));

            if (target.hits().isEmpty()) {
                return;
            }

            zombiesScene.broadcastCancellable(new EntitiesHitByGunEvent(gun, target.hits(), entity), ignored -> {
                for (ShotHandler shotHandler : shotHandlers) {
                    shotHandler.handle(gun, state, entity, previousHits, shot);
                }
            });
        });
    }

    @Override
    public void tick(@NotNull GunState state, long time) {
        for (ShotHandler handler : shotHandlers) {
            handler.tick(state, time);
        }
    }
}
