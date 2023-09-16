package org.phantazm.core.npc.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.BasicComponent;
import org.phantazm.commons.InjectionStore;

@Model("npc.interactor.command")
@Cache
public class CommandInteractor implements BasicComponent<Interactor> {
    private final Data data;

    @FactoryMethod
    public CommandInteractor(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public Interactor apply(@NotNull InjectionStore injectionStore) {
        return new Internal(data);
    }

    private record Internal(Data data) implements Interactor {
        @Override
        public void interact(@NotNull Player player) {
            MinecraftServer.getCommandManager().execute(player, data.command);
        }
    }

    @DataObject
    public record Data(@NotNull String command) {
    }
}
