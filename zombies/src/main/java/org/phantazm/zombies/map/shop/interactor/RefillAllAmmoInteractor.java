package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.inventory.InventoryAccess;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.state.InventoryKeys;

import java.util.Map;
import java.util.UUID;

@Model("zombies.map.shop.interactor.refill_all_ammo")
@Cache(false)
public class RefillAllAmmoInteractor implements ShopInteractor {
    private final Map<PlayerView, ZombiesPlayer> playerMap;

    @FactoryMethod
    public RefillAllAmmoInteractor(@NotNull Map<PlayerView, ZombiesPlayer> playerMap) {
        this.playerMap = playerMap;
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        for (ZombiesPlayer zombiesPlayer : playerMap.values()) {
            if (zombiesPlayer.hasQuit()) {
                continue;
            }

            InventoryAccess access =
                zombiesPlayer.module().getInventoryAccessRegistry().getAccess(InventoryKeys.ALIVE_ACCESS);

            for (InventoryObject object : access.profile().objects()) {
                if (object instanceof Gun gun) {
                    gun.refill();
                }
            }
        }

        return true;
    }
}
