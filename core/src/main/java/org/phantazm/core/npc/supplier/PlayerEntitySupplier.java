package org.phantazm.core.npc.supplier;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.PlayerSkin;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.entity.fakeplayer.MinimalFakePlayer;

import java.util.function.Supplier;

@Model("npc.entity.supplier.player")
@Cache
public class PlayerEntitySupplier implements Supplier<Entity> {
    private final Data data;

    @FactoryMethod
    public PlayerEntitySupplier(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public Entity get() {
        PlayerSkin playerSkin = PlayerSkin.fromUuid(data.skinUUID.replace("-", ""));
        return new MinimalFakePlayer(MinecraftServer.getSchedulerManager(), data.playerName, playerSkin);
    }

    @DataObject
    public record Data(@NotNull String playerName, @NotNull String skinUUID) {

    }
}
