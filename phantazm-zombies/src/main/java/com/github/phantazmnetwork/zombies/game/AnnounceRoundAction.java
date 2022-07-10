package com.github.phantazmnetwork.zombies.game;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.game.map.Round;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.TitlePart;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AnnounceRoundAction implements RoundAction {
    private record Data(@NotNull String formatMessage,
                        @NotNull TitlePart<Component> titlePart,
                        int priority) implements Keyed {
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "map.round.action.announce");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    private final Data data;
    private final Audience audience;

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
