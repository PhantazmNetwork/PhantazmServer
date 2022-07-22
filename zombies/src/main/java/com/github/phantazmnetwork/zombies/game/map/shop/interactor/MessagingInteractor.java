package com.github.phantazmnetwork.zombies.game.map.shop.interactor;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.Prioritized;
import com.github.phantazmnetwork.commons.component.KeyedConfigProcessor;
import com.github.phantazmnetwork.commons.component.annotation.ComponentData;
import com.github.phantazmnetwork.commons.component.annotation.ComponentFactory;
import com.github.phantazmnetwork.commons.component.annotation.ComponentModel;
import com.github.phantazmnetwork.commons.component.annotation.ComponentProcessor;
import com.github.phantazmnetwork.commons.config.PrioritizedProcessor;
import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ComponentModel("phantazm:zombies.map.shop.interactor.messaging")
public class MessagingInteractor extends InteractorBase<MessagingInteractor.Data> {
    private static final KeyedConfigProcessor<Data> PROCESSOR = new PrioritizedProcessor<>() {
        @Override
        public @NotNull Data finishData(@NotNull ConfigNode node, int priority) throws ConfigProcessException {
            List<Component> messages = AdventureConfigProcessors.component().listProcessor()
                                                                .dataFromElement(node.getElementOrThrow("messages"));
            boolean broadcast = node.getBooleanOrThrow("broadcast");
            return new Data(priority, messages, broadcast);
        }

        @Override
        public @NotNull ConfigNode finishNode(@NotNull Data data) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(2);
            node.put("messages", AdventureConfigProcessors.component().listProcessor().elementFromData(data.messages));
            node.putBoolean("broadcast", data.broadcast);
            return node;
        }
    };

    @ComponentProcessor
    public static @NotNull KeyedConfigProcessor<Data> processor() {
        return PROCESSOR;
    }

    @ComponentFactory
    public MessagingInteractor(@NotNull Data data, ZombiesMap.@NotNull Context context) {
        super(data, context);
    }

    @Override
    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        if (data.broadcast) {
            sendMessages(context.map().getInstance());
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

    @ComponentData
    public record Data(int priority, @NotNull List<Component> messages, boolean broadcast)
            implements Keyed, Prioritized {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "zombies.map.shop.interactor.messaging");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }
}
