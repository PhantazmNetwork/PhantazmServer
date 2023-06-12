package org.phantazm.core.guild;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public class Guild<TMember extends GuildMember> {

    private final GuildMemberManager<TMember> memberManager;

    private final Function<? super PlayerView, ? extends TMember> memberCreator;

    public Guild(@NotNull GuildMemberManager<TMember> memberManager,
            @NotNull Function<? super PlayerView, ? extends TMember> memberCreator) {
        this.memberManager = Objects.requireNonNull(memberManager, "memberManager");
        this.memberCreator = Objects.requireNonNull(memberCreator, "memberCreator");
    }

    public @NotNull GuildMemberManager<TMember> getMemberManager() {
        return memberManager;
    }

    public @NotNull Function<? super PlayerView, ? extends TMember> getMemberCreator() {
        return memberCreator;
    }
}
