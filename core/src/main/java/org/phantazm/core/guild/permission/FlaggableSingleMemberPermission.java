package org.phantazm.core.guild.permission;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.flag.Flaggable;
import org.phantazm.core.guild.GuildMember;

import java.util.Objects;

public class FlaggableSingleMemberPermission<TMember extends GuildMember> implements SingleMemberPermission<TMember> {

    private final Flaggable flaggable;

    private final Key flag;

    public FlaggableSingleMemberPermission(@NotNull Flaggable flaggable, @NotNull Key flag) {
        this.flaggable = Objects.requireNonNull(flaggable);
        this.flag = Objects.requireNonNull(flag);
    }

    @Override
    public boolean hasPermission(@NotNull TMember member) {
        return flaggable.hasFlag(flag);
    }
}
