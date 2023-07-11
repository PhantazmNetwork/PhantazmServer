package org.phantazm.zombies.autosplits.sound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.autosplits.event.ClientSoundCallback;
import org.phantazm.zombies.autosplits.splitter.CompositeSplitter;

import java.util.Objects;

public class AutoSplitSoundInterceptor implements ClientSoundCallback {

    private final MinecraftClient client;

    private final CompositeSplitter compositeSplitter;

    public AutoSplitSoundInterceptor(@NotNull MinecraftClient client, @NotNull CompositeSplitter compositeSplitter) {
        this.client = Objects.requireNonNull(client, "client");
        this.compositeSplitter = Objects.requireNonNull(compositeSplitter, "compositeSplitter");
    }

    @Override
    public void onPlaySound(@NotNull SoundInstance sound) {
        if (isOnHypixel() && isRoundSound(sound)) {
            compositeSplitter.split();
        }
    }

    private boolean isOnHypixel() {
        if (client.player == null) {
            return false;
        }

        String brand = client.player.getServerBrand();
        return brand != null && brand.contains("Hypixel");
    }

    private boolean isRoundSound(SoundInstance sound) {
        Identifier identifier = sound.getId();
        return identifier.equals(SoundEvents.ENTITY_WITHER_SPAWN.getId()) || identifier.equals(SoundEvents.ENTITY_ENDER_DRAGON_DEATH.getId());
    }

}
