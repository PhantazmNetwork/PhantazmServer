package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.Gun;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SendMessageEffect implements GunEffect {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.effect.send_message");

    private final Component message;

    public SendMessageEffect(@NotNull Component message) {
        this.message = Objects.requireNonNull(message, "message");
    }

    public void accept(@NotNull Gun gun) {
        gun.getOwner().getPlayer().ifPresent(player -> {
            player.sendMessage(message);
        });
    }

    @Override
    public @NotNull Key getSerialKey() {
        return SERIAL_KEY;
    }
}
