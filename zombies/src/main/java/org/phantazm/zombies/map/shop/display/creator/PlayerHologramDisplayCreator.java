package org.phantazm.zombies.map.shop.display.creator;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.VecUtils;
import org.phantazm.core.hologram.Hologram;
import org.phantazm.core.hologram.ViewableHologram;
import org.phantazm.zombies.map.HologramInfo;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.map.shop.display.HologramDisplayBase;
import org.phantazm.zombies.map.shop.display.ShopDisplay;
import org.phantazm.zombies.player.ZombiesPlayer;

@Model("zombies.map.shop.display.player.hologram")
@Cache(false)
public class PlayerHologramDisplayCreator implements PlayerDisplayCreator {
    private final Data data;

    @FactoryMethod
    public PlayerHologramDisplayCreator(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull ShopDisplay forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        return new Display(data, zombiesPlayer);
    }

    private static class Display extends HologramDisplayBase {
        private final Data data;

        private Display(Data data, ZombiesPlayer zombiesPlayer) {
            super(new ViewableHologram(Vec.ZERO, Hologram.Alignment.LOWER,
                player -> player.getUuid().equals(zombiesPlayer.getUUID())));

            this.data = data;
        }

        @Override
        public void initialize(@NotNull Shop shop) {
            hologram.setInstance(shop.instance(), shop.center().add(VecUtils.toPoint(data.info.position())));
            hologram.clear();

            hologram.addAllComponents(data.info.text());
        }
    }

    @DataObject
    public record Data(@NotNull HologramInfo info) {
    }
}
