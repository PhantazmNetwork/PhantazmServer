package com.github.phantazmnetwork.zombies.game.map.shop;

import com.github.phantazmnetwork.commons.Namespaces;
import net.kyori.adventure.key.Key;

public final class InteractionTypes {
    private InteractionTypes() {
        throw new UnsupportedOperationException();
    }

    public static final Key RIGHT_CLICK_BLOCK =
            Key.key(Namespaces.PHANTAZM, "zombies.map.shop.interaction.right_click_block");
}
