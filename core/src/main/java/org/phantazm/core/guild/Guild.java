package org.phantazm.core.guild;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Guild<TMember extends GuildMember> {

    private final Map<? super UUID, TMember> guildMembers;

    public Guild(@NotNull Map<? super UUID, TMember> guildMembers) {
        this.guildMembers = Objects.requireNonNull(guildMembers, "guildMembers");
    }

    public void addMember(@NotNull TMember guildMember) {
        guildMembers.put(guildMember.getPlayerView().getUUID(), guildMember);
    }

    public void removeMember(@NotNull UUID memberUUID) {
        guildMembers.remove(memberUUID);
    }

    public TMember getMember(@NotNull UUID memberUUID) {
        return guildMembers.get(memberUUID);
    }

    public boolean hasMember(@NotNull UUID memberUUID) {
        return getMember(memberUUID) != null;
    }

    public @NotNull Map<? super UUID, TMember> getGuildMembers() {
        return guildMembers;
    }
}
