package com.github.phantazmnetwork.zombies.mapeditor.client.ui;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.zombies.map.ShopInfo;
import com.github.phantazmnetwork.zombies.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.mapeditor.client.MapeditorSession;
import com.github.phantazmnetwork.zombies.mapeditor.client.TextPredicates;
import com.github.phantazmnetwork.zombies.mapeditor.client.TranslationKeys;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.kyori.adventure.key.Key;
import net.minecraft.text.TranslatableText;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class NewShopGui extends LightweightGuiDescription {
    public NewShopGui(@NotNull MapeditorSession session) {
        WGridPanel root = new WGridPanel();
        setRootPanel(root);

        root.setSize(100, 150);
        root.setInsets(Insets.ROOT_PANEL);

        WTextField typeName = new WTextField();
        WButton add = new WButton(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_ADD));

        Key lastSpawnrule = session.lastSpawnrule();
        typeName.setMaxLength(512);
        typeName.setText(lastSpawnrule == null ? StringUtils.EMPTY : lastSpawnrule.value());
        typeName.setTextPredicate(TextPredicates.validKeyPredicate());

        root.add(typeName, 0, 0, 5, 1);
        root.add(add, 0, 2, 5, 1);

        ZombiesMap currentMap = session.getMap();
        Vec3I firstSelected = session.getFirstSelection();
        add.setOnClick(() -> {
            String value = typeName.getText();
            if(value.isEmpty()) {
                return;
            }

            //noinspection PatternValidation
            Key typeKey = Key.key(Namespaces.PHANTAZM, typeName.getText());
            Vec3I origin = currentMap.info().origin();

            currentMap.shops().add(new ShopInfo(typeKey, Vec3I.of(firstSelected.getX() - origin.getX(),
                    firstSelected.getY() - origin.getY(), firstSelected.getZ() - origin.getZ())));
            session.refreshShops();
            ScreenUtils.closeCurrentScreen();
        });
    }
}
