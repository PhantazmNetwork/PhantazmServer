package org.phantazm.server.command.whisper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

public record WhisperConfig(@NotNull String toTargetFormat, @NotNull String toSenderFormat,
                            @NotNull TextColor fallbackNameColor, @NotNull Component consoleName,
                            @NotNull Component defaultName) {

}
