package com.github.phantazmnetwork.zombies.equipment.gun.reload.actionbar;

import net.kyori.adventure.key.Keyed;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class StaticActionBarChooser implements ReloadActionBarChooser {

    public record Data(@NotNull Component message) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.action_bar.chooser.static");

        public Data {
            Objects.requireNonNull(message, "message");
        }

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    private final Data data;

    public StaticActionBarChooser(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @NotNull Component choose(@NotNull GunState state, @NotNull Player player, float progress) {
        return data.message();
    }

    @Override
    public @NotNull Keyed getData() {
        return data;
    }
}
