package com.github.phantazmnetwork.zombies.mapeditor.client.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

/**
 * Utilities for working with {@link Screen} objects.
 */
public final class ScreenUtils {
    private ScreenUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Closes the current screen ({@code MinecraftClient.getInstance().currentScreen} if non-null.
     */
    public static void closeCurrentScreen() {
        Screen current = MinecraftClient.getInstance().currentScreen;
        if(current != null) {
            current.close();
        }
    }
}
