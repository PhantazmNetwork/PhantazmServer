package com.github.phantazmnetwork.zombies.mapeditor.client.ui;

import com.github.phantazmnetwork.zombies.mapeditor.client.TranslationKeys;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WText;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;

public class ConfirmationGui extends LightweightGuiDescription {
    public ConfirmationGui(@NotNull Text message, @NotNull Runnable onConfirm) {
        WGridPanel root = new WGridPanel();
        setRootPanel(root);

        root.setSize(100, 60);
        root.setInsets(Insets.ROOT_PANEL);

        WText display = new WText(message);
        WButton confirm = new WButton(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_YES));
        WButton deny = new WButton(new TranslatableText(TranslationKeys.GUI_MAPEDITOR_NO));

        root.add(display, 0, 0, 7, 1);
        root.add(confirm, 0, 1, 3, 1);
        root.add(deny, 4, 1, 3, 1);

        confirm.setOnClick(() -> {
            onConfirm.run();
            ScreenUtils.closeCurrentScreen();
        });

        deny.setOnClick(ScreenUtils::closeCurrentScreen);
    }


}
