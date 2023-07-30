package org.phantazm.zombies.autosplits.render;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.zombies.autosplits.splitter.internal.InternalSplitter;

import java.util.Objects;

public class RenderTimeHandler implements HudRenderCallback {

    private final MinecraftClient client;

    private InternalSplitter internalSplitter;

    private final int color;

    public RenderTimeHandler(@NotNull MinecraftClient client, int color) {
        this.client = Objects.requireNonNull(client, "client");
        this.color = color;
    }

    @Override
    public void onHudRender(DrawContext drawContext, float tickDelta) {
        if (internalSplitter == null) {
            return;
        }

        String time = getTimeString();
        int width = client.textRenderer.getWidth(time);
        Window window = client.getWindow();
        int screenWidth = window.getScaledWidth();
        int screenHeight = window.getScaledHeight();
        drawContext.getMatrices().push();
        drawContext.drawTextWithShadow(client.textRenderer, time, screenWidth - width,
                screenHeight - client.textRenderer.fontHeight, color);
        drawContext.getMatrices().pop();
    }

    private String getTimeString() {
        long millis = internalSplitter.getMillis();
        long minutesPart = millis / 60000;
        long secondsPart = (millis % 60000) / 1000;
        long tenthSecondsPart = (millis % 1000) / 100;
        return String.format("%d:%02d:%d", minutesPart, secondsPart, tenthSecondsPart);
    }

    public void setSplitter(@Nullable InternalSplitter internalSplitter) {
        this.internalSplitter = internalSplitter;
    }

}
