package com.github.phantazmnetwork.zombies.mapeditor.client.ui;

import com.github.phantazmnetwork.zombies.mapeditor.client.Identifiers;
import com.github.phantazmnetwork.zombies.mapeditor.client.TranslationKeys;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.text.TranslatableText;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class ConfigGui extends LightweightGuiDescription {
    public ConfigGui(@NotNull MapeditorViewModel viewModel) {
        WGridPanel root = new WGridPanel();
        setRootPanel(root);

        root.setSize(200, 150);
        root.setInsets(Insets.ROOT_PANEL);

        //create GUI components...
        WSprite icon = new WSprite(Identifiers.ICON);
        WToggleButton mapeditorToggle = new WToggleButton();
        WTextField mapName = new WTextField();
        WButton newMap = new WButton(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_NEW_MAP));

        //...add them to root
        root.add(icon, 0, 0, 2, 2);
        root.add(mapeditorToggle, 3, 0);
        root.add(mapName, 0, 3, 7, 0);
        root.add(newMap, 0, 5, 4, 0);

        //generic configuration
        mapName.setTextPredicate(StringUtils::isAsciiPrintable);

        //configure data bindings (two-way)
        viewModel.enabled().addListener((oldValue, newValue) -> {
            if(newValue) {
                mapeditorToggle.setToggle(true);
                mapeditorToggle.setLabel(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_ENABLED));
            }
            else {
                mapeditorToggle.setToggle(false);
                mapeditorToggle.setLabel(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_DISABLED));
            }
        });

        mapeditorToggle.setOnToggle(viewModel.enabled()::set);

        viewModel.currentMapName().addListener((oldValue, newValue) -> {
            if(newValue != null) {
                mapName.setText(newValue);
                newMap.setLabel(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_DELETE_MAP));
            }
            else {
                mapName.setText("");
                newMap.setLabel(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_NEW_MAP));
            }
        });

        newMap.setOnClick(() -> {
           String currentMap = viewModel.currentMapName().get();
           if(currentMap == null) { //create mode
               viewModel.currentMapName().set(mapName.getText());
           }
           else { //delete mode
               viewModel.currentMapName().set(null);
           }
        });
    }
}