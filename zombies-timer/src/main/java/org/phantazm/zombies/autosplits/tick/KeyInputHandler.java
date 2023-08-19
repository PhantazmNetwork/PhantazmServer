package org.phantazm.zombies.autosplits.tick;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.autosplits.splitter.CompositeSplitter;

import java.util.Objects;

public class KeyInputHandler implements ClientTickEvents.EndTick {

    private final KeyBinding keyBinding;

    private final CompositeSplitter compositeSplitter;

    public KeyInputHandler(@NotNull KeyBinding keyBinding, @NotNull CompositeSplitter compositeSplitter) {
        this.keyBinding = Objects.requireNonNull(keyBinding);
        this.compositeSplitter = Objects.requireNonNull(compositeSplitter);
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        if (!keyBinding.wasPressed()) {
            return;
        }

        Text toggledComponent;
        boolean toggled = compositeSplitter.toggle();

        PlayerEntity player = client.player;
        if (player == null) {
            return;
        }

        if (toggled) {
            toggledComponent = Text.literal("ON").formatted(Formatting.GREEN);
        } else {
            toggledComponent = Text.literal("OFF").formatted(Formatting.RED);
        }

        Text message = Text.empty()
            .formatted(Formatting.YELLOW)
            .append("Toggled AutoSplits ")
            .append(toggledComponent)
            .append("!");
        client.player.sendMessage(message, false);
    }

}
