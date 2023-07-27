package org.phantazm.zombies.map.action.round;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.toolkit.collection.Wrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.TitlePart;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.time.TickFormatter;
import org.phantazm.commons.chat.ChatDestination;
import org.phantazm.zombies.map.Round;
import org.phantazm.zombies.map.action.Action;

import java.util.Objects;

/**
 * An {@link Action} that announces the current round.
 */
@Model("zombies.map.round.action.announce")
@Cache(false)
public class AnnounceRoundAction implements Action<Round> {
    private final Data data;
    private final Instance instance;
    private final TickFormatter tickFormatter;
    private final Wrapper<Long> ticksSinceStart;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    /**
     * Creates a new instance of this class from the provided contextual data.
     *
     * @param data     the data defining the behavior of ths {@link Action}
     * @param instance the current instance
     */
    @FactoryMethod
    public AnnounceRoundAction(@NotNull Data data, @NotNull Instance instance, @NotNull Wrapper<Long> ticksSinceStart,
            @NotNull @Child("tick_formatter") TickFormatter tickFormatter) {
        this.data = Objects.requireNonNull(data, "data");
        this.instance = Objects.requireNonNull(instance, "instance");
        this.tickFormatter = Objects.requireNonNull(tickFormatter, "tickFormatter");
        this.ticksSinceStart = Objects.requireNonNull(ticksSinceStart, "ticksSinceStart");
    }

    @Override
    public void perform(@NotNull Round round) {
        TagResolver roundPlaceholder = Placeholder.component("round", Component.text(round.getRoundInfo().round()));
        String timeString = tickFormatter.format(ticksSinceStart.get());
        TagResolver timePlaceholder = Placeholder.unparsed("time", timeString);

        Component message = miniMessage.deserialize(data.format(), roundPlaceholder, timePlaceholder);
        switch (data.destination()) {
            case TITLE -> instance.sendTitlePart(TitlePart.TITLE, message);
            case SUBTITLE -> instance.sendTitlePart(TitlePart.SUBTITLE, message);
            case CHAT -> instance.sendMessage(message);
            case ACTION_BAR -> instance.sendActionBar(message);
        }
    }

    @DataObject
    public record Data(@NotNull String format,
                       @NotNull ChatDestination destination,
                       @NotNull @ChildPath("tick_formatter") String tickFormatter) {

    }

}
