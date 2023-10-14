package org.phantazm.zombies.map.action.door;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.commons.LazyComponent;
import org.phantazm.zombies.map.Door;
import org.phantazm.zombies.map.action.Action;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@Model("zombies.map.door.action.send_message")
@Cache
public class DoorSendMessageAction implements LazyComponent<ZombiesScene, Action<Door>> {
    private final Data data;

    @FactoryMethod
    public DoorSendMessageAction(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull Action<Door> apply(@NotNull InjectionStore injectionStore,
        @NotNull Supplier<@NotNull ZombiesScene> sceneSupplier) {
        return new Impl(data, sceneSupplier);
    }

    @DataObject
    public record Data(@NotNull List<Component> messages,
        boolean broadcast) {
    }

    private record Impl(Data data,
        Supplier<ZombiesScene> zombiesScene) implements Action<Door> {

        @Override
        public void perform(@NotNull Door door) {
            for (Component component : data.messages) {
                if (data.broadcast) {
                    zombiesScene.get().sendMessage(component);
                    return;
                }

                Optional<ZombiesPlayer> interactorOptional = door.lastInteractor();
                if (interactorOptional.isPresent()) {
                    interactorOptional.get().sendMessage(component);
                    return;
                }

                zombiesScene.get().sendMessage(component);
            }
        }
    }
}
