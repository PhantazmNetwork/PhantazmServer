package org.phantazm.zombies.map.action.round;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Round;
import org.phantazm.zombies.map.action.Action;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Model("zombies.map.round.action.play_sound")
public class PlaySoundAction implements Action<Round> {
    private final Data data;
    private final Random random;
    private final Map<? super UUID, ? extends ZombiesPlayer> playerMap;

    @FactoryMethod
    public PlaySoundAction(@NotNull Data data, @NotNull Random random,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap) {
        this.data = data;
        this.random = random;
        this.playerMap = playerMap;
    }

    @Override
    public void perform(@NotNull Round round) {
        for (ZombiesPlayer zombiesPlayer : playerMap.values()) {
            if (!zombiesPlayer.hasQuit()) {
                zombiesPlayer.getPlayer()
                        .ifPresent(player -> player.playSound(Sound.sound(data.sound).seed(random.nextLong()).build()));
            }
        }
    }

    @DataObject
    public record Data(@NotNull Sound sound) {
    }
}
