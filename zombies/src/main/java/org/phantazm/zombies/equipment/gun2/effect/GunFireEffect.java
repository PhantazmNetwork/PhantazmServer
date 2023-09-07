package org.phantazm.zombies.equipment.gun2.effect;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.equipment.gun2.GunModule;
import org.phantazm.zombies.equipment.gun2.Keys;
import org.phantazm.zombies.equipment.gun2.event.GunFireEvent;
import org.phantazm.zombies.equipment.perk.effect.PerkEffect;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class GunFireEffect {

    public @NotNull PerkEffect forPlayer(@NotNull ZombiesPlayer zombiesPlayer, @NotNull InjectionStore injectionStore) {
        GunModule module = injectionStore.get(Keys.GUN_MODULE);
        Effect effect = new Effect(module.gunUUID(), module.entitySupplier());
        module.eventNode().addListener(GunFireEvent.class, effect::onGunFire);

        return effect;
    }

    private static class Effect implements PerkEffect {

        private final UUID gunUUID;

        private final Supplier<Optional<? extends Entity>> entitySupplier;

        public Effect(UUID gunUUID, Supplier<Optional<? extends Entity>> entitySupplier) {
            this.gunUUID = gunUUID;
            this.entitySupplier = entitySupplier;
        }

        public void onGunFire(@NotNull GunFireEvent event) {
            if (!event.gunUUID().equals(gunUUID)) {
                return;
            }

            entitySupplier.get().ifPresent(entity -> {
                Pos start = entity.getPosition().add(0, entity.getEyeHeight(), 0);
            });
        }

    }

}
