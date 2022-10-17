package com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire;

import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunHit;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunShot;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint.ShotEndpointSelector;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler.ShotHandler;
import com.github.phantazmnetwork.zombies.equipment.gun.target.TargetFinder;
import com.github.steanky.element.core.annotation.*;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

/**
 * A {@link Firer} that fires using "hit scan". Targets will be immediately found and shot.
 */
@Model("zombies.gun.firer.hit_scan")
@Cache(false)
public class HitScanFirer implements Firer {

    private final Supplier<Optional<? extends Entity>> entitySupplier;
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
    public HitScanFirer(@NotNull Data data, @NotNull @Dependency("zombies.dependency.gun.shooter.supplier")
    Supplier<Optional<? extends Entity>> entitySupplier,
            @NotNull @DataName("end_selector") ShotEndpointSelector endSelector,
            @NotNull @DataName("target_finder") TargetFinder targetFinder,
            @NotNull @DataName("shot_handlers") Collection<ShotHandler> shotHandlers) {
        this.entitySupplier = Objects.requireNonNull(entitySupplier, "entitySupplier");
        this.endSelector = Objects.requireNonNull(endSelector, "endSelector");
        this.targetFinder = Objects.requireNonNull(targetFinder, "targetFinder");
        this.shotHandlers = List.copyOf(shotHandlers);
    }

    @Override
    public void fire(@NotNull GunState state, @NotNull Pos start, @NotNull Collection<UUID> previousHits) {
        entitySupplier.get().ifPresent(player -> {
            Optional<Point> endOptional = endSelector.getEnd(start);
            if (endOptional.isEmpty()) {
                return;
            }
            Point end = endOptional.get();

            TargetFinder.Result target = targetFinder.findTarget(player, start, end, previousHits);
            for (GunHit hit : target.regular()) {
                previousHits.add(hit.entity().getUuid());
            }
            for (GunHit hit : target.headshot()) {
                previousHits.add(hit.entity().getUuid());
            }
            for (ShotHandler shotHandler : shotHandlers) {
                shotHandler.handle(state, player, previousHits,
                        new GunShot(start, end, target.regular(), target.headshot()));
            }
        });
    }

    @Override
    public void tick(@NotNull GunState state, long time) {
        for (ShotHandler handler : shotHandlers) {
            handler.tick(state, time);
        }
    }

    /**
     * Data for a {@link HitScanFirer}.
     *
     * @param endSelectorPath  A path to the {@link HitScanFirer}'s {@link ShotEndpointSelector}
     * @param targetFinderPath A path to the {@link HitScanFirer}'s {@link TargetFinder}
     * @param shotHandlerPaths A path to the {@link HitScanFirer}'s {@link ShotHandler}s
     */
    @DataObject
    public record Data(@NotNull @DataPath("end_selector") String endSelectorPath,
                       @NotNull @DataPath("target_finder") String targetFinderPath,
                       @NotNull @DataPath("shot_handlers") Collection<String> shotHandlerPaths) {

        /**
         * Creates a {@link Data}.
         *
         * @param endSelectorPath  A path to the {@link HitScanFirer}'s {@link ShotEndpointSelector}
         * @param targetFinderPath A path to the {@link HitScanFirer}'s {@link TargetFinder}
         * @param shotHandlerPaths A path to the {@link HitScanFirer}'s {@link ShotHandler}s
         */
        public Data {
            Objects.requireNonNull(endSelectorPath, "endSelectorPath");
            Objects.requireNonNull(targetFinderPath, "targetFinderPath");
            Objects.requireNonNull(shotHandlerPaths, "shotHandlerPaths");
        }

    }

}
