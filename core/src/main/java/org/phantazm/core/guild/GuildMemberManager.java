package org.phantazm.core.guild;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class GuildMemberManager<TMember extends GuildMember> {

    private final Map<UUID, TMember> guildMembers;

    private final Map<UUID, TMember> immutableGuildMembers;

    public GuildMemberManager(@NotNull Map<UUID, TMember> guildMembers) {
        this.guildMembers = Objects.requireNonNull(guildMembers);
        this.immutableGuildMembers = Collections.unmodifiableMap(guildMembers);
    }

    public void addMember(@NotNull TMember guildMember) {
        guildMembers.put(guildMember.getPlayerView().getUUID(), guildMember);
    }

    public TMember removeMember(@NotNull UUID memberUUID) {
        return guildMembers.remove(memberUUID);
    }

    public TMember getMember(@NotNull UUID memberUUID) {
        return guildMembers.get(memberUUID);
    }

    public boolean hasMember(@NotNull UUID memberUUID) {
        return getMember(memberUUID) != null;
    }

    public @NotNull Map<UUID, TMember> getMembers() {
        return immutableGuildMembers;
    }

}
