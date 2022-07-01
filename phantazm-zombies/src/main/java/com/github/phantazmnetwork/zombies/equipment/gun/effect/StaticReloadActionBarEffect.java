package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.zombies.equipment.gun.Gun;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

public class StaticReloadActionBarEffect implements Consumer<Gun> {

    private final Component component;

    private boolean active = false;

    public StaticReloadActionBarEffect(@NotNull Component component) {
        this.component = Objects.requireNonNull(component, "component");
    }

    @Override
    public void accept(@NotNull Gun gun) {
        if (!active && !gun.canReload()) {
            gun.getOwner().getPlayer().ifPresent(player -> {
                player.sendActionBar(component);
            });
            active = true;
        }
        if (active && gun.canReload()) {
            gun.getOwner().getPlayer().ifPresent(player -> {
                player.sendActionBar(Component.empty());
            });
            active = false;
        }
    }

}
