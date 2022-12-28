package org.phantazm.zombies.map.action.round;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.TitlePart;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Round;
import org.phantazm.zombies.map.action.Action;

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
    public AnnounceRoundAction(@NotNull Data data, @NotNull Instance instance) {
        this.data = Objects.requireNonNull(data, "data");
        this.instance = Objects.requireNonNull(instance, "instane");
    }

    @Override
    public void perform(@NotNull Round round) {
        instance.sendTitlePart(data.titlePart,
                MiniMessage.miniMessage().deserialize(data.formatMessage.formatted(round.getRoundInfo().round() + 1)));
    }

    /**
     * Data for an AnnounceRoundAction.
     *
     * @param formatMessage the MiniMessage-compatible string. The format specifier %d will be replaced by the current
     *                      round number (1-indexed)
     * @param titlePart     which Component-accepting {@link TitlePart} to send the message to
     */
    @DataObject
    public record Data(@NotNull String formatMessage, @NotNull TitlePart<Component> titlePart) {
    }
}
