package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import net.kyori.adventure.key.Keyed;
import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SendMessageEffect implements GunEffect {

    public record Data(@NotNull Component message) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.effect.send_message");

        public Data {
            Objects.requireNonNull(message, "message");
        }

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }

    }

    private final Data data;

    private final PlayerView playerView;

    public SendMessageEffect(@NotNull Data data, @NotNull PlayerView playerView) {
        this.data = Objects.requireNonNull(data, "data");
        this.playerView = Objects.requireNonNull(playerView, "playerView");
    }

    @Override
    public void accept(@NotNull GunState state) {
        playerView.getPlayer().ifPresent(player -> player.sendMessage(data.message()));
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    @Override
    public @NotNull Keyed getData() {
        return data;
    }

}
