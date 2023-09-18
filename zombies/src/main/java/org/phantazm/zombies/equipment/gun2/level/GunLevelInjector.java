package org.phantazm.zombies.equipment.gun2.level;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.equipment.gun2.GunModule;
import org.phantazm.zombies.equipment.gun2.GunState;
import org.phantazm.zombies.equipment.gun2.GunStats;
import org.phantazm.zombies.equipment.gun2.Keys;
import org.phantazm.zombies.equipment.gun2.effect.GunFireEffect;
import org.phantazm.zombies.equipment.gun2.effect.GunTickEffect;
import org.phantazm.zombies.equipment.gun2.reload.BasicGunReload;
import org.phantazm.zombies.equipment.gun2.reload.GunReload;
import org.phantazm.zombies.equipment.gun2.reload.ReloadTester;
import org.phantazm.zombies.equipment.gun2.shoot.BasicGunShoot;
import org.phantazm.zombies.equipment.gun2.shoot.GunShoot;
import org.phantazm.zombies.equipment.gun2.shoot.ShootTester;
import org.phantazm.zombies.equipment.perk.effect.PerkEffect;
import org.phantazm.zombies.equipment.perk.level.PerkLevelInjector;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class GunLevelInjector implements PerkLevelInjector {

    private final GunStats stats;

    private final GunFireEffect fireEffect;

    private final GunTickEffect tickEffect = new GunTickEffect();

    public GunLevelInjector(@NotNull GunStats stats, @NotNull GunFireEffect fireEffect) {
        this.stats = Objects.requireNonNull(stats);
        this.fireEffect = Objects.requireNonNull(fireEffect);
    }

    @Override
    public void inject(InjectionStore.@NotNull Builder builder, @NotNull ZombiesPlayer zombiesPlayer, @NotNull InjectionStore oldStore) {
        UUID gunUUID = UUID.randomUUID();
        GunState state = new GunState();
        state.setHeld(false);
        state.setAmmo(stats.maxAmmo());
        state.setClip(stats.maxClip());
        state.setQueuedShots(0);
        state.setTicksSinceLastShot(stats.shootSpeed());
        state.setTicksSinceLastFire(stats.shotInterval());
        state.setTicksSinceLastReload(stats.reloadSpeed());
        state.setReloadComplete(true);

        ReloadTester reloadTester = new ReloadTester(stats, state);
        ShootTester shootTester = new ShootTester(reloadTester, stats, state, () -> 1);
        GunReload reload = new BasicGunReload(reloadTester, state);
        GunShoot shoot = new BasicGunShoot(gunUUID, shootTester, reload, stats, state);
        EventNode<Event> eventNode = oldStore.get(Keys.EVENT_NODE_HOLDER).eventNode();
        MapObjects mapObjects = oldStore.get(Keys.MAP_OBJECTS);
        GunModule module = new GunModule(gunUUID, stats, state, shootTester, reloadTester, shoot, reload, zombiesPlayer::getPlayer, eventNode, mapObjects);
        builder.with(Keys.GUN_MODULE, module);
    }

    @Override
    public @NotNull List<PerkEffect> makeDefaultEffects(@NotNull ZombiesPlayer zombiesPlayer, @NotNull InjectionStore injectionStore) {
        return List.of(fireEffect.forPlayer(zombiesPlayer, injectionStore), tickEffect.forPlayer(zombiesPlayer, injectionStore));
    }

    public record Data(@NotNull GunStats stats) {

    }
}
