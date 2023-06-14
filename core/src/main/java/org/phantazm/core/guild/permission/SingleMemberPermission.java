package org.phantazm.core.guild.permission;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.GuildMember;

public interface SingleMemberPermission<TMember extends GuildMember> {

    boolean hasPermission(@NotNull TMember member);

}
