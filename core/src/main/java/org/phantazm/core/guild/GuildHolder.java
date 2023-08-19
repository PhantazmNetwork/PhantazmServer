package org.phantazm.core.guild;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public record GuildHolder<TGuild>(@NotNull Map<? super UUID, TGuild> uuidToGuild,
    @NotNull Collection<TGuild> guilds) {

}
