package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.BlockActionPacket;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

@Model("zombies.map.shop.interactor.chest_state")
@Cache(false)
public class ChestStateInteractor implements ShopInteractor {
    private final Data data;

    private Shop shop;

    @FactoryMethod
    public ChestStateInteractor(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        this.shop = shop;
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        Chunk chunk = shop.instance().getChunkAt(shop.center());
        if (chunk == null) {
            return false;
        }

        if (data.open) {
            chunk.sendPacketToViewers(new BlockActionPacket(shop.center(), (byte)1, (byte)1, Block.CHEST));
        }
        else {
            chunk.sendPacketToViewers(new BlockActionPacket(shop.center(), (byte)1, (byte)0, Block.CHEST));
        }

        return true;
    }

    @DataObject
    public record Data(boolean open) {
    }
}
