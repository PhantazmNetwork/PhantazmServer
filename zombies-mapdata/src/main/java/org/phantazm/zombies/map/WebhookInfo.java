package org.phantazm.zombies.map;

import org.jetbrains.annotations.NotNull;

public record WebhookInfo(@NotNull String webhookURL, @NotNull String webhookFormat, @NotNull String playerFormat,
                          boolean enabled) {

    public static final WebhookInfo DEFAULT = new WebhookInfo("", "", "", false);

}
