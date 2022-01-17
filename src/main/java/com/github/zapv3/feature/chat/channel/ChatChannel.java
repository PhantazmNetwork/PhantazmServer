package com.github.zapv3.feature.chat.channel;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.jetbrains.annotations.NotNull;

public interface ChatChannel extends ForwardingAudience {

    boolean addToChannel(@NotNull Audience audience);

    boolean removeFromChannel(@NotNull Audience audience);

    boolean isInChannel(@NotNull Audience audience);

}
