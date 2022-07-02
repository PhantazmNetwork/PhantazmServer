package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.commons.Namespaces;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.RGBLike;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GradientReloadActionBarEffect extends ReloadActionBarEffect {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.effect.action_bar.reload.gradient");

    private final Component message;

    private final RGBLike from;

    private final RGBLike to;

    public GradientReloadActionBarEffect(@NotNull Component message, @NotNull RGBLike from, @NotNull RGBLike to) {
        this.message = Objects.requireNonNull(message, "message");
        this.from = Objects.requireNonNull(from, "from");
        this.to = Objects.requireNonNull(to, "to");
    }

    @Override
    protected @NotNull Component getComponent(float progress) {
        return message.color(TextColor.lerp(progress, from, to));
    }

    @Override
    public @NotNull Key getSerialKey() {
        return SERIAL_KEY;
    }
}
