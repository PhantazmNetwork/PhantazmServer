package org.phantazm.zombies.equipment.gun.reload.actionbar;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.RGBLike;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.GunState;

import java.util.Objects;

/**
 * A {@link ReloadActionBarChooser} which colors a {@link Component} based on reload progress with a gradient between
 * two colors.
 */
@Model("zombies.gun.action_bar_chooser.gradient")
@Cache
public class GradientActionBarChooser implements ReloadActionBarChooser {

    private final Data data;

    /**
     * Creates a new {@link GradientActionBarChooser} with the given {@link Data}.
     *
     * @param data The {@link Data} to use
     */
    @FactoryMethod
    public GradientActionBarChooser(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull Component choose(@NotNull GunState state, float progress) {
        return data.message().color(TextColor.lerp(progress, data.from(), data.to()));
    }

    /**
     * Data for a {@link GradientActionBarChooser}.
     *
     * @param message The message to send
     * @param from    The starting color of the gradient
     * @param to      The ending color of the gradient
     */
    @DataObject
    public record Data(@NotNull Component message,
        @NotNull RGBLike from,
        @NotNull RGBLike to) {
    }
}
