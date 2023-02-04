package org.phantazm.zombies.stage;

import com.github.steanky.toolkit.collection.Wrapper;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.corpse.Corpse;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.state.PlayerStateSwitcher;
import org.phantazm.zombies.player.state.ZombiesPlayerState;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;
import org.phantazm.zombies.player.state.context.DeadPlayerStateContext;
import org.phantazm.zombies.scoreboard.sidebar.SidebarUpdater;

import java.util.*;
import java.util.function.Function;

public class EndStage implements Stage {

    private final Instance instance;

    private final Collection<? extends ZombiesPlayer> zombiesPlayers;

    private final Map<UUID, SidebarUpdater> sidebarUpdaters = new HashMap<>();

    private final Wrapper<Long> remainingTicks;

    private final Function<? super ZombiesPlayer, ? extends SidebarUpdater> sidebarUpdaterCreator;

    public EndStage(@NotNull Instance instance, @NotNull Collection<? extends ZombiesPlayer> zombiesPlayers,
            @NotNull Wrapper<Long> remainingTicks,
            @NotNull Function<? super ZombiesPlayer, ? extends SidebarUpdater> sidebarUpdaterCreator) {
        this.instance = Objects.requireNonNull(instance, "instance");
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
        this.remainingTicks = Objects.requireNonNull(remainingTicks, "remainingTicks");
        this.sidebarUpdaterCreator = Objects.requireNonNull(sidebarUpdaterCreator, "sidebarUpdaterCreator");
    }

    public EndStage(@NotNull Instance instance, @NotNull Collection<? extends ZombiesPlayer> zombiesPlayers,
            long endTicks, Function<? super ZombiesPlayer, ? extends SidebarUpdater> sidebarUpdaterCreator) {
        this(instance, zombiesPlayers, Wrapper.of(endTicks), sidebarUpdaterCreator);
    }

    @Override
    public boolean shouldContinue() {
        return remainingTicks.get() == 0L;
    }

    @Override
    public boolean shouldRevert() {
        return false;
    }

    @Override
    public void onJoin(@NotNull ZombiesPlayer zombiesPlayer) {

    }

    @Override
    public boolean hasPermanentPlayers() {
        return true;
    }

    @Override
    public void start() {
        instance.playSound(Sound.sound(SoundEvent.ENTITY_ENDER_DRAGON_DEATH, Sound.Source.MASTER, 1.0F, 1.0F),
                Sound.Emitter.self());

        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            PlayerStateSwitcher switcher = zombiesPlayer.module().getStateSwitcher();
            ZombiesPlayerState state = switcher.getState();

            if (state.key().equals(ZombiesPlayerStateKeys.KNOCKED.key())) {
                zombiesPlayer.setState(ZombiesPlayerStateKeys.DEAD, DeadPlayerStateContext.killed(null, null));
            }
        }
    }

    @Override
    public void tick(long time) {
        remainingTicks.apply(ticks -> ticks - 1);
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            SidebarUpdater sidebarUpdater = sidebarUpdaters.computeIfAbsent(zombiesPlayer.getUUID(), unused -> {
                return sidebarUpdaterCreator.apply(zombiesPlayer);
            });
            sidebarUpdater.tick(time);
        }
    }

    @Override
    public void end() {
        for (SidebarUpdater sidebarUpdater : sidebarUpdaters.values()) {
            sidebarUpdater.end();
        }
    }

    @Override
    public @NotNull Key key() {
        return StageKeys.END;
    }
}
