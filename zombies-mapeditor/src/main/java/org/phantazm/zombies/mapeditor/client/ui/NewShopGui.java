package org.phantazm.zombies.mapeditor.client.ui;

import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.vector.Vec3I;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Namespaces;
import org.phantazm.zombies.map.Evaluation;
import org.phantazm.zombies.map.MapInfo;
import org.phantazm.zombies.map.ShopInfo;
import org.phantazm.zombies.mapeditor.client.EditorSession;

import java.util.Objects;

/**
 * General UI for creating new {@link ShopInfo} instances.
 */
public class NewShopGui extends NamedObjectGui {
    /**
     * Constructs a new instance of this GUI, which allows a user to create shops of a specific type.
     *
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
            if (value.isEmpty()) {
                return;
            }

            Key typeKey = Key.key(Namespaces.PHANTAZM, value);
            Vec3I origin = currentMap.settings().origin();

            currentMap.shops().add(new ShopInfo(typeKey, firstSelected.sub(origin), Evaluation.ALL_TRUE,
                    new LinkedConfigNode(0)));
            session.refreshShops();
            ScreenUtils.closeCurrentScreen();
        });
    }
}
