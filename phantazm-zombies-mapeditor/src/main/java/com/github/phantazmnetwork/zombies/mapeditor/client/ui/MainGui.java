package com.github.phantazmnetwork.zombies.mapeditor.client.ui;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.mapeditor.client.Identifiers;
import com.github.phantazmnetwork.zombies.mapeditor.client.MapeditorSession;
import com.github.phantazmnetwork.zombies.mapeditor.client.TranslationKeys;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class MainGui extends LightweightGuiDescription {
    public MainGui(@NotNull MapeditorSession session) {
        WGridPanel root = new WGridPanel();
        setRootPanel(root);

        root.setSize(200, 150);
        root.setInsets(Insets.ROOT_PANEL);

        //create GUI components...
        WSprite icon = new WSprite(Identifiers.ICON);
        WToggleButton mapeditorToggle = new WToggleButton();
        WTextField mapNameBox = new WTextField();
        WButton newMap = new WButton(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_NEW_MAP));
        WText feedback = new WText(new LiteralText(StringUtils.EMPTY));

        //...add them to root
        root.add(icon, 0, 0, 2, 2);
        root.add(mapeditorToggle, 3, 0);
        root.add(mapNameBox, 0, 2, 7, 0);
        root.add(newMap, 0, 4, 4, 0);
        root.add(feedback, 0, 5, 5, 0);

        //generic configuration
        updateMapeditorToggle(mapeditorToggle, session.isEnabled());

        mapNameBox.setMaxLength(512);
        mapNameBox.setTextPredicate(string -> {
            try {
                Key.key(Namespaces.PHANTAZM, string);
                return true;
            }
            catch (InvalidKeyException ignored) {
                return false;
            }
        });

        //events
        mapeditorToggle.setOnToggle(toggled -> {
            session.setEnabled(toggled);
            updateMapeditorToggle(mapeditorToggle, toggled);
        });

        newMap.setOnClick(() -> {
            String name = mapNameBox.getText();
            if(name.isEmpty()) {
                feedback.setText(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_FEEDBACK_EMPTY_MAP_NAME));
                return;
            }

            if(!session.hasSelection()) {
                feedback.setText(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_FEEDBACK_NO_SELECTION));
                return;
            }

            MinecraftClient.getInstance().setScreen(new MapeditorScreen(new NewMapGui(session, name)));
        });
    }

    private void updateMapeditorToggle(WToggleButton mapeditorToggle, boolean enabled) {
        mapeditorToggle.setToggle(enabled);
        mapeditorToggle.setLabel(enabled ? new TranslatableText(TranslationKeys.GUI_MAPEDITOR_ENABLED) :
                new TranslatableText(TranslationKeys.GUI_MAPEDITOR_DISABLED));
    }
}