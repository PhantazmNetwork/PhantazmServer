package com.github.phantazmnetwork.zombies.stage;

import com.github.phantazmnetwork.commons.Activable;
import com.github.phantazmnetwork.commons.Wrapper;
import com.github.phantazmnetwork.core.time.TickFormatter;
import com.github.phantazmnetwork.zombies.map.RoundHandler;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.player.state.ZombiesPlayerStateKeys;
import it.unimi.dsi.fastutil.longs.LongList;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class BasicStages {

    private BasicStages() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Stage idle(@NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers) {
        Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");

        return new Stage.Builder().setContinueCondition(() -> !zombiesPlayers.isEmpty()).setRevertCondition(() -> false)
                .setPermanentPlayers(false).build();
    }

    public static @NotNull Stage countdown(@NotNull Instance instance,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers, @NotNull LongList alertTicks,
            @NotNull TickFormatter tickFormatter, long countdownTicks) {
        Objects.requireNonNull(instance, "instance");
        Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
        Objects.requireNonNull(alertTicks, "alertTicks");
        Objects.requireNonNull(tickFormatter, "tickFormatter");

        Wrapper<Long> ticksRemaining = Wrapper.of(countdownTicks);

        return new Stage.Builder().setContinueCondition(() -> ticksRemaining.get() == 0L)
                .setRevertCondition(zombiesPlayers::isEmpty).setPermanentPlayers(false).addActivable(new Activable() {
                    @Override
                    public void tick(long time) {
                        if (alertTicks.contains(time)) {
                            instance.sendMessage(Component.textOfChildren(Component.text("The game starts in"),
                                    tickFormatter.format(ticksRemaining.get()), Component.text(".")));
                        }

                        ticksRemaining.apply(ticks -> ticks - 1);
                    }
                }).build();
    }

    public static @NotNull Stage inGame(@NotNull Instance instance,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers, @NotNull Pos spawnPos,
            @NotNull RoundHandler roundHandler) {
        Objects.requireNonNull(instance, "instance");
        Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
        Objects.requireNonNull(spawnPos, "spawnPos");
        Objects.requireNonNull(roundHandler, "roundHandler");

        Wrapper<Long> ticksSinceStart = Wrapper.of(0L);

        return new Stage.Builder().setContinueCondition(roundHandler::hasEnded).setRevertCondition(() -> false)
                .setPermanentPlayers(true).addActivable(new Activable() {
                    @Override
                    public void start() {
                        for (ZombiesPlayer zombiesPlayer : zombiesPlayers.values()) {
                            zombiesPlayer.getPlayer().ifPresent(player -> {
                                player.teleport(spawnPos);
                            });
                        }
                        ticksSinceStart.set(0L);
                        roundHandler.setCurrentRound(0);
                    }

                    @Override
                    public void tick(long time) {
                        ticksSinceStart.apply(ticks -> ticks + 1);
                    }

                    @Override
                    public void end() {
                        boolean anyAlive = false;
                        for (ZombiesPlayer zombiesPlayer : zombiesPlayers.values()) {
                            if (zombiesPlayer.isState(ZombiesPlayerStateKeys.ALIVE)) {
                                anyAlive = true;
                                break;
                            }
                        }

                        if (anyAlive) {
                            instance.sendMessage(Component.text("You won"));
                        }
                        else {
                            instance.sendMessage(Component.text("You lost"));
                        }
                    }
                }).build();
    }

    public static @NotNull Stage end(@NotNull Instance instance, long endTicks) {
        Wrapper<Long> remainingTicks = Wrapper.of(endTicks);

        return new Stage.Builder().setContinueCondition(() -> remainingTicks.get() == 0L)
                .setRevertCondition(() -> false).setPermanentPlayers(true).addActivable(new Activable() {
                    @Override
                    public void start() {
                        instance.playSound(
                                Sound.sound(SoundEvent.ENTITY_ENDER_DRAGON_DEATH, Sound.Source.MASTER, 1.0F, 1.0F),
                                Sound.Emitter.self());
                    }

                    @Override
                    public void tick(long time) {
                        remainingTicks.apply(ticks -> ticks - 1);
                    }
                }).build();
    }

}
