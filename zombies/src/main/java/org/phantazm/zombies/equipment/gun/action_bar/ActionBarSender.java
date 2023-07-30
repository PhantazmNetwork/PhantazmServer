package org.phantazm.zombies.equipment.gun.action_bar;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ActionBarSender {

    void sendActionBar(@NotNull Component message);

}
