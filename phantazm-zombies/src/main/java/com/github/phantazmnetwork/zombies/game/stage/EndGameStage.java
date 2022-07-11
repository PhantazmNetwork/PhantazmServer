package com.github.phantazmnetwork.zombies.game.stage;

import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class EndGameStage implements Stage {

    private final Map<UUID, ZombiesPlayer> zombiesPlayers;

    private final long endGameDuration;

    private long endGameTicksRemaining;

    public EndGameStage(@NotNull Map<UUID, ZombiesPlayer> zombiesPlayers, long endGameDuration) {
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
        this.endGameDuration = endGameDuration;
        this.endGameTicksRemaining = endGameDuration;
    }

    @Override
    public void tick(long time) {
        if (endGameDuration == endGameTicksRemaining--) {
            Sound sound = Sound.sound(
                    SoundEvent.ENTITY_ENDER_DRAGON_DEATH.key(),
                    Sound.Source.MASTER,
                    1.0F,
                    1.0F
            );
            for (ZombiesPlayer zombiesPlayer : zombiesPlayers.values()) {
                zombiesPlayer.getPlayerView().getPlayer().ifPresent(player -> {
                    player.playSound(sound);
                });
            }
        }
    }

    @Override
    public void start() {
        endGameTicksRemaining = endGameDuration;
    }

    @Override
    public void end() {

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
