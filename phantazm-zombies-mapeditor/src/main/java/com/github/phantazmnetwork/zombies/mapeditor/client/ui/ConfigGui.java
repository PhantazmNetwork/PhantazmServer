package com.github.phantazmnetwork.zombies.mapeditor.client.ui;

import com.github.phantazmnetwork.zombies.mapeditor.client.Identifiers;
import com.github.phantazmnetwork.zombies.mapeditor.client.MapeditorSession;
import com.github.phantazmnetwork.zombies.mapeditor.client.TranslationKeys;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ConfigGui extends LightweightGuiDescription {
    private final MapeditorSession mapeditorSession;

    public ConfigGui(@NotNull MapeditorSession mapeditorSession) {
        super();
        this.mapeditorSession = Objects.requireNonNull(mapeditorSession, "mapeditorSession");
        boolean initiallyEnabled = mapeditorSession.isEnabled();

        WGridPanel root = new WGridPanel();
        setRootPanel(root);

        root.setSize(300, 240);
        root.setInsets(Insets.ROOT_PANEL);

        WSprite icon = new WSprite(Identifiers.ICON);
        root.add(icon, 0, 0, 2, 2);

        WButton enableButton = new WButton();
        if(initiallyEnabled) {
            enableButton.setLabel(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_DISABLE));
        }
        else {
            enableButton.setLabel(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_ENABLE));
        }

        enableButton.setOnClick(() -> {
            if(!mapeditorSession.isEnabled()) {
                mapeditorSession.setEnabled(true);
                enableButton.setLabel(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_DISABLE));
            }
            else {
                mapeditorSession.setEnabled(false);
                enableButton.setLabel(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_ENABLE));
            }
        });

        root.add(enableButton, 3, 0, 5, 1);

        WButton toggleViews = new WButton(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_TOGGLE_VIEWS));
        toggleViews.setOnClick(() -> {
            MinecraftClient.getInstance().setScreen(new MapeditorScreen(new ViewsGui(), true));
        });

        root.add(toggleViews, 9, 0, 5, 1);

        WGridPanel scrollGrid = new WGridPanel();
        scrollGrid.setSize(300, 240);
        scrollGrid.setInsets(Insets.ROOT_PANEL);

        WScrollPanel scrollPanel = new WScrollPanel(scrollGrid);
        root.add(scrollPanel, 0, 3, 17, 9);

        root.add(new WButton(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_ADD)), 0, 13, 5,
                0);

        root.validate(this);
    }

    private class ViewsGui extends LightweightGuiDescription {
        public ViewsGui() {
            WGridPanel root = new WGridPanel();
            root.setSize(300, 240);
            root.setInsets(Insets.ROOT_PANEL);

            setRootPanel(root);

            WButton button = new WButton();
            root.add(button, 0, 0, 1, 5);

            root.validate(this);
        }
    }
}