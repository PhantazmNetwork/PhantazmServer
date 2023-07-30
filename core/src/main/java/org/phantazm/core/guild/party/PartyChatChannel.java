package org.phantazm.core.guild.party;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectBooleanPair;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChatEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.chat.BasicChatChannel;
import org.phantazm.core.chat.ChatChannel;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class PartyChatChannel extends BasicChatChannel {

    public static final String CHANNEL_NAME = "party";

    private final Map<? super UUID, ? extends Party> parties;

    public PartyChatChannel(@NotNull Map<? super UUID, ? extends Party> parties,
            @NotNull PlayerViewProvider viewProvider, @NotNull MiniMessage miniMessage, @NotNull String chatFormat) {
        super(viewProvider, miniMessage, chatFormat);
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

}
