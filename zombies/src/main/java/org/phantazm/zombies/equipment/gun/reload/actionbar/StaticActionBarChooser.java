package org.phantazm.zombies.equipment.gun.reload.actionbar;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.GunState;

import java.util.Objects;

/**
 * A {@link ReloadActionBarChooser} that sends a static {@link Component} message.
 */
@Model("zombies.gun.action_bar_chooser.static")
@Cache
public class StaticActionBarChooser implements ReloadActionBarChooser {

    private final Data data;

    /**
     * Creates a {@link StaticActionBarChooser}.
     *
     * @param data The {@link StaticActionBarChooser}'s {@link Data}
     */
    @FactoryMethod
    public StaticActionBarChooser(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @NotNull Component choose(@NotNull GunState state, float progress) {
        return data.message();
    }

    /**
     * Data for a {@link StaticActionBarChooser}.
     *
     * @param message The message to send
     */
    @DataObject
    public record Data(@NotNull Component message) {

        /**
         * Creates a {@link Data}.
         *
         * @param message The message to send
         */
        public Data {
            Objects.requireNonNull(message, "message");
        }

    }

}
