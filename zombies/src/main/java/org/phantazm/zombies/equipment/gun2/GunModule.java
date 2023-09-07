package org.phantazm.zombies.equipment.gun2;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun2.reload.GunReload;
import org.phantazm.zombies.equipment.gun2.reload.ReloadTester;
import org.phantazm.zombies.equipment.gun2.shoot.GunShoot;
import org.phantazm.zombies.equipment.gun2.shoot.ShootTester;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public record GunModule(@NotNull UUID gunUUID,
    @NotNull GunStats stats,
    @NotNull GunState state,
    @NotNull ShootTester shootTester,
    @NotNull ReloadTester reloadTester,
    @NotNull GunShoot shoot,
    @NotNull GunReload reload,
    @NotNull Supplier<Optional<? extends Entity>> entitySupplier,
    @NotNull EventNode<Event> eventNode) {

}
