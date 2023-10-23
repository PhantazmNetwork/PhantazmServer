package org.phantazm.core.guild.permission;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.GuildMember;

import java.util.Collection;
import java.util.Objects;

public class OrMultipleMemberPermission<TMember extends GuildMember> implements SingleMemberPermission<TMember> {

    private final Collection<SingleMemberPermission<TMember>> children;

    public OrMultipleMemberPermission(@NotNull Collection<SingleMemberPermission<TMember>> children) {
        this.children = Objects.requireNonNull(children);
    }

    @Override
    public boolean hasPermission(@NotNull TMember member) {
        for (SingleMemberPermission<TMember> child : children) {
            if (child.hasPermission(member)) {
                return true;
            }
        }

        return false;
    }
}
