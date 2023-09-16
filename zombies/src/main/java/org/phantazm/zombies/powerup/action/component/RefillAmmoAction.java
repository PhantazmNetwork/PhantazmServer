package org.phantazm.zombies.powerup.action.component;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;
import org.phantazm.zombies.powerup.action.InstantAction;
import org.phantazm.zombies.powerup.action.PowerupAction;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Map;

@Model("zombies.powerup.action.refill_ammo")
@Cache(false)
public class RefillAmmoAction implements PowerupActionComponent {
    @FactoryMethod
    public RefillAmmoAction() {
    }

    @Override
    public @NotNull PowerupAction apply(@NotNull ZombiesScene scene) {
        return new Action(scene.managedPlayers());
    }

    private static class Action extends InstantAction {
        private final Map<PlayerView, ZombiesPlayer> zombiesPlayers;

        private Action(Map<PlayerView, ZombiesPlayer> zombiesPlayers) {
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
