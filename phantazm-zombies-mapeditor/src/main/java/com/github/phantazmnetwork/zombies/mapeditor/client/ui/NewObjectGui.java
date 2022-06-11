package com.github.phantazmnetwork.zombies.mapeditor.client.ui;

import com.github.phantazmnetwork.zombies.mapeditor.client.MapeditorSession;
import com.github.phantazmnetwork.zombies.mapeditor.client.TranslationKeys;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;

public class NewObjectGui extends LightweightGuiDescription {
    public NewObjectGui(@NotNull MapeditorSession session) {
        WGridPanel root = new WGridPanel();
        setRootPanel(root);

        root.setSize(100, 150);
        root.setInsets(Insets.ROOT_PANEL);

        WButton newRoom = new WButton(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_NEW_ROOM));
        WButton newDoor = new WButton(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_NEW_DOOR));

        root.add(newRoom, 0, 0, 5, 1);
        root.add(newDoor, 0, 1, 5, 1);

        newRoom.setOnClick(() -> MinecraftClient.getInstance().setScreen(new MapeditorScreen(new NewRoomGui(session))));
        newDoor.setOnClick(() -> MinecraftClient.getInstance().setScreen(new MapeditorScreen(new NewDoorGui(session))));
    }
}
