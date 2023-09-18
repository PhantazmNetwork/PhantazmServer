package org.phantazm.zombies.equipment.gun2.effect;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.equipment.gun2.GunModule;
import org.phantazm.zombies.equipment.gun2.Keys;
import org.phantazm.zombies.equipment.gun2.event.GunFireEvent;
import org.phantazm.zombies.equipment.gun2.shoot.fire.Firer;
import org.phantazm.zombies.equipment.perk.effect.PerkEffect;
import org.phantazm.zombies.player.PlayerComponent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.*;
import java.util.function.Supplier;

public class GunFireEffect {

    private final PlayerComponent<Firer> firer;

    public GunFireEffect(@NotNull PlayerComponent<Firer> firer) {
        this.firer = Objects.requireNonNull(firer);
    }

    public @NotNull PerkEffect forPlayer(@NotNull ZombiesPlayer zombiesPlayer, @NotNull InjectionStore injectionStore) {
        GunModule module = injectionStore.get(Keys.GUN_MODULE);
        Effect effect = new Effect(module.gunUUID(), module.entitySupplier(), firer.forPlayer(zombiesPlayer, injectionStore));
        module.eventNode().addListener(GunFireEvent.class, effect::onGunFire);

        return effect;
    }

    private static class Effect implements PerkEffect {

        private final UUID gunUUID;

        private final Supplier<Optional<? extends Entity>> entitySupplier;

        private final Firer firer;

        public Effect(UUID gunUUID, Supplier<Optional<? extends Entity>> entitySupplier, Firer firer) {
            this.gunUUID = gunUUID;
            this.entitySupplier = entitySupplier;
            this.firer = firer;
        }

        public void onGunFire(@NotNull GunFireEvent event) {
            if (!event.gunUUID().equals(gunUUID)) {
                return;
            }

            entitySupplier.get().ifPresent(entity -> {
                Pos start = entity.getPosition().add(0, entity.getEyeHeight(), 0);
                Collection<UUID> previousHits = new ArrayList<>();
                firer.fire(start, previousHits);
            });
        }

    }

}
