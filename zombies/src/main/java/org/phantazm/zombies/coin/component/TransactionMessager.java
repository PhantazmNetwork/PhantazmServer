package org.phantazm.zombies.coin.component;

import net.kyori.adventure.text.Component;
import net.minestom.server.Tickable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface TransactionMessager extends Tickable {

    void sendMessage(@NotNull List<Component> displays, int change);

}
