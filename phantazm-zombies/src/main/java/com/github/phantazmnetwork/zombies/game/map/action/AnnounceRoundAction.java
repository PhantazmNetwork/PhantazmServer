package com.github.phantazmnetwork.zombies.game.map.action;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.game.map.Round;
import com.github.phantazmnetwork.commons.component.KeyedConfigProcessor;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.TitlePart;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An {@link Action} that announces the current round.
 */
public class AnnounceRoundAction implements Action<Round> {
    /**
     * Data for an AnnounceRoundAction.
     * @param formatMessage the MiniMessage-compatible string. The format specifier %i will be replaced by the current
     *                      round number (1-indexed)
     * @param titlePart which Component-accepting {@link TitlePart} to send the message to
     * @param priority the priority of this action; actions with higher priority will be executed first
     */
    public record Data(@NotNull String formatMessage,
                        @NotNull TitlePart<Component> titlePart,
                        int priority) implements Keyed {
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "zombies.map.round.action.announce");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    private static final KeyedConfigProcessor<Data> PROCESSOR = new KeyedConfigProcessor<>() {
        @Override
        public @NotNull Key key() {
            return Data.SERIAL_KEY;
        }

        @Override
        public Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            String formatMessage = element.getStringOrThrow("formatMessage");
            TitlePart<Component> titlePart = AdventureConfigProcessors.componentTitlePart().dataFromElement(element
                    .getElementOrThrow("titlePart"));
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

    public static @NotNull KeyedConfigProcessor<Data> processor() {
        return PROCESSOR;
    }

    private final Data data;
    private final Audience audience;

    /**
     * Creates a new instance of this class from the provided data, which will announce to the given {@link Audience}.
     * @param data the data defining the behavior of ths {@link Action}
     * @param audience the audience to announce to
     */
    public AnnounceRoundAction(@NotNull Data data, @NotNull Audience audience) {
        this.data = Objects.requireNonNull(data, "data");
        this.audience = Objects.requireNonNull(audience, "audience");
    }

    @Override
    public void perform(@NotNull Round round) {
        audience.sendTitlePart(data.titlePart, MiniMessage.miniMessage().deserialize(data.formatMessage.formatted(round
                .getData().round())));
    }

    @Override
    public int priority() {
        return data.priority;
    }
}