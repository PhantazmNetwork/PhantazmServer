package org.phantazm.zombies.equipment.gun2.shoot.fire;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.equipment.gun2.GunModule;
import org.phantazm.zombies.equipment.gun2.Keys;
import org.phantazm.zombies.equipment.gun2.event.GunHitShotEvent;
import org.phantazm.zombies.equipment.gun2.shoot.GunHit;
import org.phantazm.zombies.equipment.gun2.shoot.GunShot;
import org.phantazm.zombies.equipment.gun2.target.TargetFinder;
import org.phantazm.zombies.player.PlayerComponent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class HitScanFirer implements PlayerComponent<Firer> {

    @Override
    public @NotNull Firer forPlayer(@NotNull ZombiesPlayer player, @NotNull InjectionStore injectionStore) {
        GunModule module = injectionStore.get(Keys.GUN_MODULE);
        return new Impl(module.entitySupplier());
    }

    private static class Impl implements Firer {

        private final Supplier<Optional<? extends Entity>> entitySupplier;

        public Impl(Supplier<Optional<? extends Entity>> entitySupplier) {
            this.entitySupplier = entitySupplier;
        }

        @Override
        public void fire(@NotNull Pos start, @NotNull Collection<UUID> previousHits) {
            entitySupplier.get().ifPresent(entity -> {
                Optional<Point> endOptional = endSelector.getEnd(start);
                if (endOptional.isEmpty()) {
                    return;
                }
                Point end = endOptional.get();

                TargetFinder.Result target = targetFinder.findTarget(entity, start, end, previousHits);
                for (GunHit hit : target.regular()) {
                    previousHits.add(hit.entity().getUuid());
                }
                for (GunHit hit : target.headshot()) {
                    previousHits.add(hit.entity().getUuid());
                }

                GunShot shot = new GunShot(start, end, target.regular(), target.headshot());
                EventDispatcher.call(new GunHitShotEvent(shot));
            });
        }
    }

}
