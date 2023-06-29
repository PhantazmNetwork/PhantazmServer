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
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.sound.SongLoader;
import org.phantazm.core.sound.SongPlayer;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

import java.util.List;
import java.util.Optional;

@Model("zombies.map.shop.interactor.play_song")
@Cache(false)
public class PlaySongInteractor implements ShopInteractor {
    private final Data data;
    private final SongLoader songLoader;
    private final SongPlayer songPlayer;

    private Shop shop;

    @FactoryMethod
    public PlaySongInteractor(@NotNull Data data, @NotNull SongLoader songLoader, @NotNull SongPlayer songPlayer) {
        this.data = data;
        this.songLoader = songLoader;
        this.songPlayer = songPlayer;
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        this.shop = shop;
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        if (!songLoader.songs().contains(data.songKey)) {
            return false;
        }

        List<SongPlayer.Note> notes = songLoader.getNotes(data.songKey);
        Optional<Player> playerOptional = interaction.player().getPlayer();
        if (data.broadcast) {
            playerOptional.ifPresent(player -> {
                Instance instance = player.getInstance();
                if (instance != null) {
                    if (data.atLocation) {
                        songPlayer.play(instance, shop.center(), notes);
                    }
                    else {
                        songPlayer.play(instance, Sound.Emitter.self(), notes);
                    }
                }
            });
        }
        else {
            if (data.atLocation) {
                playerOptional.ifPresent(player -> songPlayer.play(player, shop.center(), notes));
            }
            else {
                playerOptional.ifPresent(player -> songPlayer.play(player, Sound.Emitter.self(), notes));
            }
        }

        return true;
    }

    @DataObject
    public record Data(@NotNull Key songKey, boolean broadcast, boolean atLocation) {
        @Default("broadcast")
        public static @NotNull ConfigElement broadcastDefault() {
            return ConfigPrimitive.of(false);
        }

        @Default("atLocation")
        public static @NotNull ConfigElement atLocationDefault() {
            return ConfigPrimitive.of(true);
        }
    }
}
