package com.github.phantazmnetwork.zombies.game.map.shop.interactor;

import com.github.phantazmnetwork.commons.ConfigProcessors;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@Model("zombies.map.shop.interactor.messaging")
public class MessagingInteractor extends InteractorBase<MessagingInteractor.Data> {
    private final Instance instance;

    @FactoryMethod
    public MessagingInteractor(@NotNull Data data,
            @NotNull @Dependency("zombies.dependency.map_object.instance") Instance instance) {
        super(data);
        this.instance = Objects.requireNonNull(instance, "instance");
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            private static final ConfigProcessor<List<Component>> COMPONENT_LIST_PROCESSOR =
                    ConfigProcessors.component().listProcessor();

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                List<Component> messages =
                        COMPONENT_LIST_PROCESSOR.dataFromElement(element.getElementOrThrow("messages"));
                boolean broadcast = element.getBooleanOrThrow("broadcast");
                return new Data(messages, broadcast);
            }

            @Override
            public @NotNull ConfigNode elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(2);
                node.put("messages", COMPONENT_LIST_PROCESSOR.elementFromData(data.messages));
                node.putBoolean("broadcast", data.broadcast);
                return node;
            }
        };
    }

    @Override
    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        if (data.broadcast) {
            sendMessages(instance);
        }
        else {
            interaction.getPlayer().getPlayerView().getPlayer().ifPresent(this::sendMessages);
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
