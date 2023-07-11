package org.phantazm.zombies.autosplits.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.sound.SoundInstance;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ClientSoundCallback {

    Event<ClientSoundCallback> EVENT = EventFactory.createArrayBacked(ClientSoundCallback.class, callbacks -> (sound) -> {
        for (ClientSoundCallback callback : callbacks) {
            callback.onPlaySound(sound);
        }
    });

    void onPlaySound(@NotNull SoundInstance sound);

}
