package com.github.phantazmnetwork.zombies.game.map.action.round;

import com.github.phantazmnetwork.commons.ConfigProcessors;
import com.github.phantazmnetwork.zombies.game.map.Round;
import com.github.phantazmnetwork.zombies.game.map.action.Action;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.TitlePart;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An {@link Action} that announces the current round.
 */
@Model("zombies.map.round.action.announce")
public class AnnounceRoundAction implements Action<Round> {
    private final Data data;
    private final Instance instance;

    /**
     * Creates a new instance of this class from the provided contextual data.
     *
     * @param data     the data defining the behavior of ths {@link Action}
     * @param instance the current instance
     */
    @FactoryMethod
    public AnnounceRoundAction(@NotNull Data data,
            @NotNull @Dependency("zombies.dependency.map_object.instance") Instance instance) {
        this.data = Objects.requireNonNull(data, "data");
        this.instance = Objects.requireNonNull(instance, "instane");
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            private static final ConfigProcessor<TitlePart<Component>> TITLE_PART_CONFIG_PROCESSOR =
                    ConfigProcessors.componentTitlePart();

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement node) throws ConfigProcessException {
                String formatMessage = node.getStringOrThrow("formatMessage");
                TitlePart<Component> titlePart =
                        TITLE_PART_CONFIG_PROCESSOR.dataFromElement(node.getElementOrThrow("titlePart"));
                return new Data(formatMessage, titlePart);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(2);
                node.putString("formatMessage", data.formatMessage);
                node.put("titlePart", TITLE_PART_CONFIG_PROCESSOR.elementFromData(data.titlePart));
                return node;
            }
        };
    }

    @Override
    public void perform(@NotNull Round round) {
        instance.sendTitlePart(data.titlePart,
                MiniMessage.miniMessage().deserialize(data.formatMessage.formatted(round.getData().round())));
    }

    /**
     * Data for an AnnounceRoundAction.
     *
     * @param formatMessage the MiniMessage-compatible string. The format specifier %i will be replaced by the current
     *                      round number (1-indexed)
     * @param titlePart     which Component-accepting {@link TitlePart} to send the message to
     */
    @DataObject
    public record Data(@NotNull String formatMessage, @NotNull TitlePart<Component> titlePart) {
    }
}
