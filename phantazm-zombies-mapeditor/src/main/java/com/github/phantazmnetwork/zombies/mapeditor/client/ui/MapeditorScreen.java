package com.github.phantazmnetwork.zombies.mapeditor.client.ui;

import io.github.cottonmc.cotton.gui.GuiDescription;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MapeditorScreen extends CottonClientScreen {
    private final Screen parent;

    public MapeditorScreen(@NotNull GuiDescription description) {
        super(description);
        this.parent = null;
    }

    public MapeditorScreen(@NotNull GuiDescription description, @Nullable Screen parent) {
        super(description);
        this.parent = parent;
    }

    @Override
    public void close() {
        super.close();

        if(parent != null) {
            MinecraftClient.getInstance().setScreen(parent);
        }
    }
}
