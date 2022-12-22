package org.phantazm.zombies.map.shop;

import net.kyori.adventure.key.Key;
import org.phantazm.commons.Namespaces;

public final class InteractionTypes {
    public static final Key RIGHT_CLICK_BLOCK =
            Key.key(Namespaces.PHANTAZM, "zombies.map.shop.interaction.right_click_block");

    public static final Key RIGHT_CLICK_ENTITY =
            Key.key(Namespaces.PHANTAZM, "zombies.map.shop.interaction.right_click_entity");

    public static final Key CLICK_INVENTORY =
            Key.key(Namespaces.PHANTAZM, "zombies.map.shop.interaction.click_inventory");

    public static final Key COLLIDE = Key.key(Namespaces.PHANTAZM, "zombies.map.shop.interaction.collide");

    private InteractionTypes() {
        throw new UnsupportedOperationException();
    }
}
