package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.sound.SongLoader;
import org.phantazm.core.sound.SongPlayer;
import org.phantazm.zombies.map.shop.PlayerInteraction;

import java.util.List;

@Model("zombies.map.shop.interactor.play_song")
@Cache(false)
public class PlaySongInteractor implements ShopInteractor {
    private final Data data;
    private final SongLoader songLoader;
    private final SongPlayer songPlayer;

    @FactoryMethod
    public PlaySongInteractor(@NotNull Data data, @NotNull SongLoader songLoader, @NotNull SongPlayer songPlayer) {
        this.data = data;
        this.songLoader = songLoader;
        this.songPlayer = songPlayer;
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        if (!songLoader.songs().contains(data.songKey)) {
            return false;
        }

        List<SongPlayer.Note> notes = songLoader.getNotes(data.songKey);
        if (data.broadcast) {
            interaction.player().getPlayer().ifPresent(player -> {
                Instance instance = player.getInstance();
                if (instance != null) {
                    songPlayer.play(instance, Sound.Emitter.self(), notes);
                }
            });
        }
        else {
            interaction.player().getPlayer().ifPresent(player -> songPlayer.play(player, player, notes));
        }

        return true;
    }

    @DataObject
    public record Data(@NotNull Key songKey, boolean broadcast) {
        @Default("broadcast")
        public static @NotNull ConfigElement broadcastDefault() {
            return ConfigPrimitive.of(false);
        }
    }
}
