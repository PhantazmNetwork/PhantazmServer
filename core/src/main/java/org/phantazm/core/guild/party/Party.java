package org.phantazm.core.guild.party;

import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.Guild;
import org.phantazm.core.guild.GuildMember;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class Party extends Guild<GuildMember> implements PacketGroupingAudience {
    public Party(@NotNull Map<? super UUID, GuildMember> guildMembers) {
        super(guildMembers);
    }

    @Override
    public @NotNull Collection<@NotNull Player> getPlayers() {
        Collection<Player> players = new ArrayList<>();
        for (GuildMember member : getGuildMembers().values()) {
            member.getPlayerView().getPlayer().ifPresent(players::add);
        }

        return players;
    }

}
