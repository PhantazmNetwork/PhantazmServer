package com.github.phantazmnetwork.zombies.mapeditor.client.ui;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.vector.Region3I;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.zombies.map.*;
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

public class NewSpawnpointGui extends LightweightGuiDescription {
    public NewSpawnpointGui(@NotNull MapeditorSession session) {
        WGridPanel root = new WGridPanel();
        setRootPanel(root);

        root.setSize(100, 150);
        root.setInsets(Insets.ROOT_PANEL);

        WTextField spawnruleName = new WTextField();
        WButton add = new WButton(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_ADD));

        Key lastSpawnrule = session.lastSpawnrule();
        spawnruleName.setMaxLength(512);
        spawnruleName.setText(lastSpawnrule == null ? StringUtils.EMPTY : lastSpawnrule.value());
        spawnruleName.setTextPredicate(TextPredicates.validKeyPredicate());

        root.add(spawnruleName, 0, 0, 5, 1);
        root.add(add, 0, 2, 5, 1);

        ZombiesMap currentMap = session.getMap();
        Vec3I firstSelected = session.getFirstSelection();
        add.setOnClick(() -> {
            String value = spawnruleName.getText();
            if(value.isEmpty()) {
                return;
            }

            //noinspection PatternValidation
            Key spawnruleKey = Key.key(Namespaces.PHANTAZM, spawnruleName.getText());

            currentMap.spawnpoints().add(new SpawnpointInfo(firstSelected, spawnruleKey));
            session.refreshSpawnpoints();
            ScreenUtils.closeCurrentScreen();
        });
    }
}
