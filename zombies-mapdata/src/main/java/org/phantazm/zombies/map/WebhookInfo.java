package org.phantazm.zombies.map;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import org.jetbrains.annotations.NotNull;

public record WebhookInfo(
    @NotNull String webhookURL,
    @NotNull String webhookFormat,
    @NotNull String playerFormat,
    @NotNull String noModifierPlaceholder,
    boolean enabled) {

    public static final WebhookInfo DEFAULT = new WebhookInfo("", "", "", "`None`", false);

    @Default("noModifierPlaceholder")
    public static @NotNull ConfigElement defaultNoModifierPlaceholder() {
        return ConfigPrimitive.of("`None`");
    }
}
