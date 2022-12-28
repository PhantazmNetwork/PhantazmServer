package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;

import java.util.List;
import java.util.Objects;

@Model("zombies.map.shop.interactor.messaging")
public class MessagingInteractor extends InteractorBase<MessagingInteractor.Data> {
    private final Instance instance;

    @FactoryMethod
    public MessagingInteractor(@NotNull Data data, @NotNull Instance instance) {
        super(data);
        this.instance = Objects.requireNonNull(instance, "instance");
    }

    @Override
    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        if (data.broadcast) {
            sendMessages(instance);
        }
        else {
            interaction.player().getModule().getPlayerView().getPlayer().ifPresent(this::sendMessages);
        }
    }

    private void sendMessages(Audience audience) {
        for (Component component : data.messages) {
            audience.sendMessage(component);
        }
    }

    @DataObject
    public record Data(@NotNull List<Component> messages, boolean broadcast) {
    }
}
