package org.phantazm.zombies.map.action.door;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Door;
import org.phantazm.zombies.map.action.Action;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.List;
import java.util.Objects;

@Model("zombies.map.door.action.send_message")
public class DoorSendMessageAction implements Action<Door> {
    private final Data data;
    private final Instance instance;

    @FactoryMethod
    public DoorSendMessageAction(@NotNull Data data, @NotNull Instance instance) {
        this.data = Objects.requireNonNull(data, "data");
        this.instance = Objects.requireNonNull(instance, "instance");
    }

    @Override
    public void perform(@NotNull Door door) {
        for (Component component : data.messages) {
            if (data.broadcast) {
                instance.sendMessage(component);
            }
            else {
                ZombiesPlayer interactor = door.lastInteractor();
                if (interactor != null) {
                    interactor.sendMessage(component);
                }
                else {
                    instance.sendMessage(component);
                }
            }
        }
    }

    @DataObject
    public record Data(@NotNull List<Component> messages, boolean broadcast) {
    }
}
