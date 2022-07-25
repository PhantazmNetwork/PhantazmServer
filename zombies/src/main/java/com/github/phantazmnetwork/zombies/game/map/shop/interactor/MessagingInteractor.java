package com.github.phantazmnetwork.zombies.game.map.shop.interactor;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.Prioritized;
import com.github.phantazmnetwork.commons.config.PrioritizedProcessor;
import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ElementModel("zombies.map.shop.interactor.messaging")
public class MessagingInteractor extends InteractorBase<MessagingInteractor.Data> {
    private static final ConfigProcessor<Data> PROCESSOR = new PrioritizedProcessor<>() {
        private static final ConfigProcessor<List<Component>> COMPONENT_LIST_PROCESSOR =
                AdventureConfigProcessors.component().listProcessor();

        @Override
        public @NotNull Data finishData(@NotNull ConfigNode node, int priority) throws ConfigProcessException {
            List<Component> messages = COMPONENT_LIST_PROCESSOR.dataFromElement(node.getElementOrThrow("messages"));
            boolean broadcast = node.getBooleanOrThrow("broadcast");
            return new Data(priority, messages, broadcast);
        }

        @Override
        public @NotNull ConfigNode finishNode(@NotNull Data data) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(2);
            node.put("messages", COMPONENT_LIST_PROCESSOR.elementFromData(data.messages));
            node.putBoolean("broadcast", data.broadcast);
            return node;
        }
    };

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return PROCESSOR;
    }

    @FactoryMethod
    public MessagingInteractor(@NotNull Data data,
                               @NotNull @ElementDependency("zombies.dependency.map") ZombiesMap map) {
        super(data, map);
    }

    @Override
    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        if (data.broadcast) {
            sendMessages(map.getInstance());
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

    @ElementData
    public record Data(int priority, @NotNull List<Component> messages, boolean broadcast)
            implements Keyed, Prioritized {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "zombies.map.shop.interactor.messaging");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }
}
