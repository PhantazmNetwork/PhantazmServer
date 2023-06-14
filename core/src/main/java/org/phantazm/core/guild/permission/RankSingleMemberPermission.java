package org.phantazm.core.guild.permission;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.GuildMember;
import org.phantazm.core.guild.Ranked;

public class RankSingleMemberPermission<TMember extends GuildMember & Ranked> implements SingleMemberPermission<TMember> {

    private final int minimumExecutionRank;

    public RankSingleMemberPermission(int minimumExecutionRank) {
        this.minimumExecutionRank = minimumExecutionRank;
    }

    @Override
    public boolean hasPermission(@NotNull TMember member) {
        return member.rank() >= minimumExecutionRank;
    }
}
