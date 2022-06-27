package com.github.phantazmnetwork.zombies.mapeditor.client.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

public final class ScreenUtils {
    private ScreenUtils() {
        throw new UnsupportedOperationException();
    }

    public static void closeCurrentScreen() {
        Screen current = MinecraftClient.getInstance().currentScreen;
        if(current != null) {
            current.close();
        }
    }
}
