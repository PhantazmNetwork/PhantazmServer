package org.phantazm.zombies.equipment.gun2.target.tester;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.equipment.gun2.Keys;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.PlayerComponent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Collection;
import java.util.UUID;

public class MobTargetTester implements PlayerComponent<TargetTester> {
    @Override
    public @NotNull TargetTester forPlayer(@NotNull ZombiesPlayer player, @NotNull InjectionStore injectionStore) {
        return new Tester(injectionStore.get(Keys.GUN_MODULE).mapObjects());
    }

    public static class Tester implements TargetTester {

        private final MapObjects mapObjects;

        public Tester(MapObjects mapObjects) {
            this.mapObjects = mapObjects;
        }

        @Override
        public boolean useTarget(@NotNull Entity target, @NotNull Collection<UUID> previousHits) {
            return mapObjects.module().roundHandlerSupplier().get().currentRound()
                .map(round -> round.hasMob(target.getUuid())).orElse(false);
        }
    }
}
