package com.github.phantazmnetwork.zombies.mapeditor.client.ui;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.zombies.map.ShopInfo;
import com.github.phantazmnetwork.zombies.map.MapInfo;
import com.github.phantazmnetwork.zombies.mapeditor.client.EditorSession;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * General UI for creating new {@link ShopInfo} instances.
 */
public class NewShopGui extends NamedObjectGui {
    /**
     * Constructs a new instance of this GUI, which allows a user to create shops of a specific type.
     * @param session the current {@link EditorSession}
     */
    @SuppressWarnings("PatternValidation")
    public NewShopGui(@NotNull EditorSession session) {
        super(null);

        Objects.requireNonNull(session, "session");

        MapInfo currentMap = session.getMap();
        Vec3I firstSelected = session.getFirstSelection();
        buttonAdd.setOnClick(() -> {
            String value = textFieldName.getText();
            if(value.isEmpty()) {
                return;
            }

            Key typeKey = Key.key(Namespaces.PHANTAZM, value);
            Vec3I origin = currentMap.info().origin();

            currentMap.shops().add(new ShopInfo(typeKey, Vec3I.of(firstSelected.getX() - origin.getX(),
                    firstSelected.getY() - origin.getY(), firstSelected.getZ() - origin.getZ())));
            session.refreshShops();
            ScreenUtils.closeCurrentScreen();
        });
    }
}
