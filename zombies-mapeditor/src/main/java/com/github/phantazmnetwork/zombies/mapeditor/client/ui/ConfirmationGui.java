package com.github.phantazmnetwork.zombies.mapeditor.client.ui;

import com.github.phantazmnetwork.zombies.mapeditor.client.TranslationKeys;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A general confirmation dialog, allowing the user to choose "yes" or "no", and displaying a specific query.
 */
public class ConfirmationGui extends SimplePanelGui {
    /**
     * Creates a new ConfirmationGui with the provided query message and Runnable to execute if the user selects "yes".
     *
     * @param message   the query message
     * @param onConfirm the routine to execute if the user chooses "yes"
     */
    public ConfirmationGui(@NotNull Text message, @NotNull Runnable onConfirm) {
        super(100, 60);

        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(onConfirm, "onConfirm");

        WText display = new WText(message);
        WButton confirm = new WButton(Text.translatable(TranslationKeys.GUI_MAPEDITOR_YES));
        WButton deny = new WButton(Text.translatable(TranslationKeys.GUI_MAPEDITOR_NO));

        gridPanelRoot.add(display, 0, 0, 7, 1);
        gridPanelRoot.add(confirm, 0, 1, 3, 1);
        gridPanelRoot.add(deny, 4, 1, 3, 1);

        confirm.setOnClick(() -> {
            onConfirm.run();
            ScreenUtils.closeCurrentScreen();
        });

        deny.setOnClick(ScreenUtils::closeCurrentScreen);
    }
}
