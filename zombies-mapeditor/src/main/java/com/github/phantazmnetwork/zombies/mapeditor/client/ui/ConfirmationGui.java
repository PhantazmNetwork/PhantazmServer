package com.github.phantazmnetwork.zombies.mapeditor.client.ui;

import com.github.phantazmnetwork.zombies.mapeditor.client.TranslationKeys;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A general confirmation dialog, allowing the user to choose "yes" or "no", and displaying a specific query.
 */
public class ConfirmationGui extends SimplePanelGui {
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

    public ConfirmationGui(@NotNull Text message, @NotNull Runnable onConfirm, @NotNull Runnable onDeny) {
        this(message, onConfirm, onDeny, 100, computeHeight(message, 100));
    }

    @SuppressWarnings("SameParameterValue")
    private static int computeHeight(Text message, int width) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        return textRenderer.wrapLines(message, width).size() * textRenderer.fontHeight + 30;
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
}
