package org.phantazm.core.guild.permission;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.GuildMember;

public interface MultipleMemberPermission<TMember extends GuildMember> {

    boolean hasPermission(@NotNull TMember executor);

    boolean canExecute(@NotNull TMember executor, @NotNull TMember target);

}
