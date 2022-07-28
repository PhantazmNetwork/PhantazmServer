package com.github.phantazmnetwork.zombies.game.map.action.round;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.game.map.Round;
import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.game.map.action.Action;
import com.github.steanky.element.core.annotation.*;
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

import java.util.Objects;

/**
 * An {@link Action} that announces the current round.
 */
@ElementModel("zombies.map.round.action.announce")
public class AnnounceRoundAction implements Action<Round> {
    private static final ConfigProcessor<Data> PROCESSOR = new ConfigProcessor<>() {
        private static final ConfigProcessor<TitlePart<Component>> TITLE_PART_CONFIG_PROCESSOR =
                AdventureConfigProcessors.componentTitlePart();

        @Override
        public @NotNull Data dataFromElement(@NotNull ConfigElement node) throws ConfigProcessException {
            String formatMessage = node.getStringOrThrow("formatMessage");
            TitlePart<Component> titlePart =
                    TITLE_PART_CONFIG_PROCESSOR.dataFromElement(node.getElementOrThrow("titlePart"));
            int priority = node.getNumberOrThrow("priority").intValue();
            return new Data(formatMessage, titlePart, priority);
        }

        @Override
        public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(3);
            node.putString("formatMessage", data.formatMessage);
            node.put("titlePart", TITLE_PART_CONFIG_PROCESSOR.elementFromData(data.titlePart));
            node.putNumber("priority", data.priority);
            return node;
        }
    };
    private final Data data;
    private final Audience audience;
    /**
     * Creates a new instance of this class from the provided contextual data.
     *
     * @param data the data defining the behavior of ths {@link Action}
     * @param map  the current map
     */
    @FactoryMethod
    public AnnounceRoundAction(@NotNull Data data,
            @NotNull @ElementDependency("zombies.dependency.map") ZombiesMap map) {
        this.data = Objects.requireNonNull(data, "data");
        this.audience = map.getInstance();
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return PROCESSOR;
    }

    @Override
    public void perform(@NotNull Round round) {
        audience.sendTitlePart(data.titlePart,
                MiniMessage.miniMessage().deserialize(data.formatMessage.formatted(round.getData().round())));
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
    @ElementData
    public record Data(@NotNull String formatMessage, @NotNull TitlePart<Component> titlePart, int priority)
            implements Keyed {
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "zombies.map.round.action.announce");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }
}
