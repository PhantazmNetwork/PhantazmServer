package com.github.phantazmnetwork.zombies.game.map.shop;

import com.github.phantazmnetwork.commons.Namespaces;
import net.kyori.adventure.key.Key;

public final class InteractionTypes {
    public static final Key RIGHT_CLICK =
            Key.key(Namespaces.PHANTAZM, "zombies.map.shop.interaction.right_click_block");

    private InteractionTypes() {
        throw new UnsupportedOperationException();
    }
}
