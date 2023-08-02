package org.phantazm.zombies.powerup2.action;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup2.Powerup;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@Model("zombies.powerup.action.refill_ammo")
@Cache(false)
public class RefillAmmoAction implements PowerupActionComponent {
    @FactoryMethod
    public RefillAmmoAction() {
    }

    @Override
    public @NotNull PowerupAction apply(@NotNull ZombiesScene scene) {
        return new Action(scene.getZombiesPlayers());
    }

    private static class Action extends InstantAction {
        private final Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers;

        private Action(Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers) {
            this.zombiesPlayers = zombiesPlayers;
        }

        @Override
        public void activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer player, long time) {
            for (ZombiesPlayer zombiesPlayer : zombiesPlayers.values()) {
                if (zombiesPlayer.hasQuit()) {
                    continue;
                }

                zombiesPlayer.module().getEquipmentHandler().accessRegistry().getCurrentAccess().ifPresent(access -> {
                    for (InventoryObject object : access.profile().objects()) {
                        if (object instanceof Gun gun) {
                            gun.refill();
                        }
                    }
                });
            }
        }
    }
}
