package org.phantazm.server.validator;

import it.unimi.dsi.fastutil.booleans.BooleanObjectPair;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface LoginValidator {
    Component NOT_WHITELISTED_MESSAGE = Component.text("You are not whitelisted on this server!");

    @NotNull BooleanObjectPair<Component> validateLogin(@NotNull UUID uuid);

    void ban(@NotNull UUID uuid, @NotNull Component reason);

    boolean isBanned(@NotNull UUID uuid);

    void pardon(@NotNull UUID uuid);

    void addWhitelist(@NotNull UUID uuid);

    boolean isWhitelisted(@NotNull UUID uuid);

    void removeWhitelist(@NotNull UUID uuid);

    void flush();
}
