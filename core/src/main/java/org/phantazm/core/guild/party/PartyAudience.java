package org.phantazm.core.guild.party;

import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.GuildMember;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class PartyAudience implements PacketGroupingAudience {

    private final Collection<? extends GuildMember> guildMembers;

    public PartyAudience(@NotNull Collection<? extends GuildMember> guildMembers) {
        this.guildMembers = Objects.requireNonNull(guildMembers);
    }

    @Override
    public @NotNull Collection<@NotNull Player> getPlayers() {
        Collection<Player> players = new ArrayList<>(guildMembers.size());
        for (GuildMember member : guildMembers) {
            member.getPlayerView().getPlayer().ifPresent(players::add);
        }

        return players;
    }

}
