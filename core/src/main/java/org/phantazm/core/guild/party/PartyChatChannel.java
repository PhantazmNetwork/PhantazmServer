package org.phantazm.core.guild.party;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectBooleanPair;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChatEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.chat.BasicChatChannel;
import org.phantazm.core.chat.ChatChannel;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class PartyChatChannel extends BasicChatChannel {

    public static final String CHANNEL_NAME = "party";

    private final Map<? super UUID, ? extends Party> parties;

    public PartyChatChannel(@NotNull Map<? super UUID, ? extends Party> parties,
            @NotNull PlayerViewProvider viewProvider) {
        super(viewProvider);
        this.parties = Objects.requireNonNull(parties, "parties");
    }

    @Override
    protected @NotNull Pair<Audience, ObjectBooleanPair<Component>> getAudience(@NotNull Player player) {
        Party party = parties.get(player.getUuid());
        if (party == null) {
            return Pair.of(null,
                    ObjectBooleanPair.of(Component.text("You are not in a party", NamedTextColor.RED), true));
        }

        return Pair.of(party.getAudience(), null);
    }

    @Override
    public @NotNull Component formatMessage(@NotNull PlayerChatEvent chatEvent) {
        return Component.textOfChildren(Component.text("Party", NamedTextColor.BLUE),
                Component.text(" > ", NamedTextColor.DARK_GRAY), super.formatMessage(chatEvent));
    }
}
