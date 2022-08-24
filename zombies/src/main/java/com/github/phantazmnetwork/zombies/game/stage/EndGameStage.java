package com.github.phantazmnetwork.zombies.game.stage;

import com.github.phantazmnetwork.commons.Activable;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class EndGameStage extends StageBase {

    private final Collection<ZombiesPlayer> zombiesPlayers;

    private final long endGameDuration;

    private long endGameTicksRemaining;

    public EndGameStage(@NotNull Collection<Activable> activables, @NotNull Collection<ZombiesPlayer> zombiesPlayers,
            long endGameDuration) {
        super(activables);
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
        this.endGameDuration = endGameDuration;
        this.endGameTicksRemaining = endGameDuration;
    }

    @Override
    public void tick(long time) {
        super.tick(time);
        endGameTicksRemaining--;
    }

    @Override
    public void start() {
        super.start();
        endGameTicksRemaining = endGameDuration;
        Sound sound = Sound.sound(SoundEvent.ENTITY_ENDER_DRAGON_DEATH.key(), Sound.Source.MASTER, 1.0F, 1.0F);
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            zombiesPlayer.getPlayerView().getPlayer().ifPresent(player -> {
                player.playSound(sound);
            });
        }
    }

    @Override
    public boolean shouldEnd() {
        return endGameTicksRemaining <= 0;
    }

    @Override
    public boolean hasPermanentPlayers() {
        return true;
    }
}
