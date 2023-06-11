package org.phantazm.core.guild;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Guild {

    private final Map<? super UUID, GuildMember> guildMembers;

    public Guild(@NotNull Map<? super UUID, GuildMember> guildMembers) {
        this.guildMembers = Objects.requireNonNull(guildMembers, "guildMembers");
    }

    public void addMember(@NotNull GuildMember guildMember) {
        guildMembers.put(guildMember.getPlayerView().getUUID(), guildMember);
    }

    public void removeMember(@NotNull UUID memberUUID) {
        guildMembers.remove(memberUUID);
    }

    public @NotNull Map<? super UUID, GuildMember> getGuildMembers() {
        return guildMembers;
    }
}
