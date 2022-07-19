package com.github.phantazmnetwork.zombies.game.map.action.round;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.component.KeyedConfigProcessor;
import com.github.phantazmnetwork.commons.component.annotation.ComponentData;
import com.github.phantazmnetwork.commons.component.annotation.ComponentFactory;
import com.github.phantazmnetwork.commons.component.annotation.ComponentModel;
import com.github.phantazmnetwork.commons.component.annotation.ComponentProcessor;
import com.github.phantazmnetwork.zombies.game.map.Round;
import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.game.map.action.Action;
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
@ComponentModel("phantazm:zombies.map.round.action.announce")
public class AnnounceRoundAction implements Action<Round> {
    private static final KeyedConfigProcessor<Data> PROCESSOR = new KeyedConfigProcessor<>() {
        @Override
        public @NotNull Data dataFromNode(@NotNull ConfigNode node) throws ConfigProcessException {
            String formatMessage = node.getStringOrThrow("formatMessage");
            TitlePart<Component> titlePart =
                    AdventureConfigProcessors.componentTitlePart().dataFromElement(node.getElementOrThrow("titlePart"));
            int priority = node.getNumberOrThrow("priority").intValue();
            return new Data(formatMessage, titlePart, priority);
        }

        @Override
        public @NotNull ConfigNode nodeFromData(Data data) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(3);
            node.putString("formatMessage", data.formatMessage);
            node.put("titlePart", AdventureConfigProcessors.componentTitlePart().elementFromData(data.titlePart));
            node.putNumber("priority", data.priority);
            return node;
        }
    };

    @ComponentProcessor
    public static @NotNull KeyedConfigProcessor<Data> processor() {
        return PROCESSOR;
    }

    private final Data data;
    private final Audience audience;

    /**
     * Creates a new instance of this class from the provided contextual data.
     *
     * @param data    the data defining the behavior of ths {@link Action}
     * @param context the context of this action
     */
    @ComponentFactory
    public AnnounceRoundAction(@NotNull Data data, @NotNull ZombiesMap.Context context) {
        this.data = Objects.requireNonNull(data, "data");
        this.audience = Objects.requireNonNull(context.map().getInstance(), "context.instance");
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
    @ComponentData
    public record Data(@NotNull String formatMessage, @NotNull TitlePart<Component> titlePart, int priority)
            implements Keyed {
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "zombies.map.round.action.announce");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }
}
