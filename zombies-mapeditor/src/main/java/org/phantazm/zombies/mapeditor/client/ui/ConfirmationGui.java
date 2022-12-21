package org.phantazm.zombies.mapeditor.client.ui;

import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.mapeditor.client.TranslationKeys;

import java.util.Objects;

/**
 * A general confirmation dialog, allowing the user to choose "yes" or "no", and displaying a specific query.
 */
public class ConfirmationGui extends SimplePanelGui {
    /**
     * Creates a new ConfirmationGui with the supplied message, action to run on confirmation, action to run on denial,
     * width, and height.
     *
     * @param message   the message to display
     * @param onConfirm the action to run if the user confirms
     * @param onDeny    the action to run if the user denies
     * @param width     the width of the dialog
     * @param height    the height of the dialog
     */
    public ConfirmationGui(@NotNull Text message, @NotNull Runnable onConfirm, @NotNull Runnable onDeny, int width,
            int height) {
        super(width, height);

        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(onConfirm, "onConfirm");
        Objects.requireNonNull(onDeny, "onDeny");

        WText display = new WText(message);
        WButton confirm = new WButton(Text.translatable(TranslationKeys.GUI_MAPEDITOR_YES));
        WButton deny = new WButton(Text.translatable(TranslationKeys.GUI_MAPEDITOR_NO));

        int textGridHeight = (int)Math.ceil((double)height / grid);
        gridPanelRoot.add(display, 0, 0, 7, textGridHeight);
        gridPanelRoot.add(confirm, 0, textGridHeight, 3, 1);
        gridPanelRoot.add(deny, 4, textGridHeight, 3, 1);

        confirm.setOnClick(onConfirm);
        deny.setOnClick(onDeny);
    }

    /**
     * Creates a new ConfirmationGui with the supplied message, action to run on confirmation, action to run on denial,
     * and a default width/height of 100.
     *
     * @param message   the message to display
     * @param onConfirm the action to run if the user confirms
     * @param onDeny    the action to run if the user denies
     */
    public ConfirmationGui(@NotNull Text message, @NotNull Runnable onConfirm, @NotNull Runnable onDeny) {
        this(message, onConfirm, onDeny, 100, computeHeight(message, 100));
    }

    /**
     * Creates a new ConfirmationGui with the provided query message and Runnable to execute if the user selects "yes".
     *
     * @param message   the query message
     * @param onConfirm the routine to execute if the user chooses "yes"
     */
    public ConfirmationGui(@NotNull Text message, @NotNull Runnable onConfirm) {
        this(message, onConfirm, ScreenUtils::closeCurrentScreen);
    }

    @SuppressWarnings("SameParameterValue")
    private static int computeHeight(Text message, int width) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        return textRenderer.wrapLines(message, width).size() * textRenderer.fontHeight + 30;
    }
}
