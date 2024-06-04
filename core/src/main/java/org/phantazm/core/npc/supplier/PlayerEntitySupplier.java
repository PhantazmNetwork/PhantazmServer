package org.phantazm.core.npc.supplier;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.PlayerSkin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.commons.MonoComponent;
import org.phantazm.core.entity.fakeplayer.MinimalFakePlayer;

import java.util.function.Supplier;

@Model("npc.entity.supplier.player")
@Cache
public class PlayerEntitySupplier implements MonoComponent<Supplier<Entity>> {
    private final Data data;

    @FactoryMethod
    public PlayerEntitySupplier(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public Supplier<Entity> apply(@NotNull InjectionStore injectionStore) {
        return new Internal(data);
    }

    private record Internal(Data data) implements Supplier<Entity> {
        @Override
        public Entity get() {
            if (data.skinUUID != null) {
                return new MinimalFakePlayer(MinecraftServer.getSchedulerManager(), data.playerName,
                    PlayerSkin.fromUuid(data.skinUUID.replace("-", "")));
            }

            return new MinimalFakePlayer(MinecraftServer.getSchedulerManager(), data.playerName, data.playerSkin);
        }
    }

    @Default("""
        {
          playerName='',
          skinUUID=null,
          playerSkin=null
        }
        """)
    @DataObject
    public record Data(@NotNull String playerName,
        @Nullable String skinUUID,
        @Nullable PlayerSkin playerSkin) {
    }
}
