package com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire;

import net.kyori.adventure.key.Keyed;
import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunHit;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunShot;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint.ShotEndpointSelector;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler.ShotHandler;
import com.github.phantazmnetwork.zombies.equipment.gun.target.TargetFinder;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class HitScanFirer implements Firer {

    public record Data(@NotNull Key endSelectorKey, @NotNull Key targetFinderKey,
                       @NotNull Collection<Key> shotHandlerKeys) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.firer.hit_scan");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    private final Data data;

    private final PlayerView playerView;

    private final ShotEndpointSelector endSelector;

    private final TargetFinder targetFinder;

    private final Collection<ShotHandler> shotHandlers;

    public HitScanFirer(@NotNull Data data, @NotNull PlayerView playerView, @NotNull ShotEndpointSelector endSelector,
                        @NotNull TargetFinder targetFinder, @NotNull Collection<ShotHandler> shotHandlers) {
        this.data = Objects.requireNonNull(data, "data");
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.endSelector = Objects.requireNonNull(endSelector, "endSelector");
        this.targetFinder = Objects.requireNonNull(targetFinder, "targetFinder");
        this.shotHandlers = Objects.requireNonNull(shotHandlers, "shotHandlers");
    }

    @Override
    public void fire(@NotNull GunState state, @NotNull Pos start, @NotNull Collection<PhantazmMob> previousHits) {
        playerView.getPlayer().ifPresent(player -> {
            Optional<Point> endOptional = endSelector.getEnd(start);
            if (endOptional.isEmpty()) {
                return;
            }
            Point end = endOptional.get();

            TargetFinder.Result target = targetFinder.findTarget(player, start, end, previousHits);
            for (GunHit hit : target.regular()) {
                previousHits.add(hit.mob());
            }
            for (GunHit hit : target.headshot()) {
                previousHits.add(hit.mob());
            }
            for (ShotHandler shotHandler : shotHandlers) {
                shotHandler.handle(state, player, previousHits, new GunShot(start, end, target.regular(),
                        target.headshot()));
            }
        });
    }

    @Override
    public void tick(@NotNull GunState state, long time) {
        for (ShotHandler handler : shotHandlers) {
            handler.tick(state, time);
        }
    }

    @Override
    public @NotNull Keyed getData() {
        return data;
    }
}
