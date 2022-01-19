package com.github.phantazmnetwork.api.channel;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Basic implementation of a {@link ChatChannel}.
 */
public class BaseChatChannel implements ChatChannel {

    //TODO Audience shouldn't be stored in a set like this, no guaranteed proper impl of equals/hashcode
    private final Set<Audience> audiences = new HashSet<>();

    @Override
    public boolean addToChannel(@NotNull Audience audience) {
        Objects.requireNonNull(audience, "audience");
        return audiences.add(audience);
    }

    @Override
    public boolean removeFromChannel(@NotNull Audience audience) {
        Objects.requireNonNull(audience, "audience");
        return audiences.remove(audience);
    }

    @Override
    public boolean isInChannel(@NotNull Audience audience) {
        Objects.requireNonNull(audience, "audience");
        return audiences.contains(audience);
    }

    @Override
    public @NotNull Iterable<? extends Audience> audiences() {
        return audiences;
    }

}
