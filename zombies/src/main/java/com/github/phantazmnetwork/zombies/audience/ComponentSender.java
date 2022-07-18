package com.github.phantazmnetwork.zombies.audience;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface ComponentSender {

    void send(@NotNull Audience audience, @NotNull Component component);

}
