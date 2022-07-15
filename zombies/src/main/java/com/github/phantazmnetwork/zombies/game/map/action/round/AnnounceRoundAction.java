package com.github.phantazmnetwork.zombies.game.map.action.round;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.component.DependencyProvider;
import com.github.phantazmnetwork.commons.component.KeyedFactory;
import com.github.phantazmnetwork.commons.component.annotation.ComponentFactory;
import com.github.phantazmnetwork.commons.component.annotation.ComponentModel;
import com.github.phantazmnetwork.commons.component.annotation.ComponentProcessor;
import com.github.phantazmnetwork.zombies.game.map.Round;
import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.game.map.action.Action;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.TitlePart;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;

/**
 * An {@link Action} that announces the current round.
 */
@ComponentModel("phantazm:zombies.map.round.action.announce")
public class AnnounceRoundAction implements Action<Round> {
    private static final ConfigProcessor<Data> PROCESSOR = new ConfigProcessor<>() {
        @Override
        public Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            String formatMessage = element.getStringOrThrow("formatMessage");
            TitlePart<Component> titlePart = AdventureConfigProcessors.componentTitlePart().dataFromElement(
                    element.getElementOrThrow("titlePart"));
            int priority = element.getNumberOrThrow("priority").intValue();
            return new Data(formatMessage, titlePart, priority);
        }

        @Override
        public @NotNull ConfigElement elementFromData(Data data) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(3);
            node.putString("formatMessage", data.formatMessage);
            node.put("titlePart", AdventureConfigProcessors.componentTitlePart().elementFromData(data.titlePart));
            node.putNumber("priority", data.priority);
            return node;
        }
    };

    private static final KeyedFactory<Data, AnnounceRoundAction> FACTORY = new KeyedFactory<>() {
        private static final List<Key> DEPENDENCIES = List.of(ZombiesMap.Context.DEPENDENCY_KEY);

        @Override
        public @NotNull AnnounceRoundAction make(@NotNull DependencyProvider dependencyProvider, @NotNull Data data) {
            ZombiesMap.Context context = dependencyProvider.provide(ZombiesMap.Context.DEPENDENCY_KEY);
            return new AnnounceRoundAction(data, context.instance());
        }

        @Override
        public @Unmodifiable @NotNull List<Key> dependencies() {
            return DEPENDENCIES;
        }
    };

    private final Data data;
    private final Audience audience;

    /**
     * Creates a new instance of this class from the provided data, which will announce to the given {@link Audience}.
     *
     * @param data     the data defining the behavior of ths {@link Action}
     * @param audience the audience to announce to
     */
    public AnnounceRoundAction(@NotNull Data data, @NotNull Audience audience) {
        this.data = Objects.requireNonNull(data, "data");
        this.audience = Objects.requireNonNull(audience, "audience");
    }

    @ComponentProcessor
    public static @NotNull ConfigProcessor<Data> processor() {
        return PROCESSOR;
    }

    @ComponentFactory
    public static @NotNull KeyedFactory<Data, AnnounceRoundAction> factory() {
        return FACTORY;
    }

    @Override
    public void perform(@NotNull Round round) {
        audience.sendTitlePart(data.titlePart, MiniMessage.miniMessage().deserialize(
                data.formatMessage.formatted(round.getData().round())));
    }

    @Override
    public int priority() {
        return data.priority;
    }

    /**
     * Data for an AnnounceRoundAction.
     *
     * @param formatMessage the MiniMessage-compatible string. The format specifier %i will be replaced by the current
     *                      round number (1-indexed)
     * @param titlePart     which Component-accepting {@link TitlePart} to send the message to
     * @param priority      the priority of this action; actions with higher priority will be executed first
     */
    public record Data(@NotNull String formatMessage, @NotNull TitlePart<Component> titlePart, int priority)
            implements Keyed {
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "zombies.map.round.action.announce");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }
}
