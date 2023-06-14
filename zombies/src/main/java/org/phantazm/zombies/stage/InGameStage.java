package org.phantazm.zombies.stage;

import com.github.steanky.toolkit.collection.Wrapper;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.TitlePart;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.EquipmentHandler;
import org.phantazm.core.time.TickFormatter;
import org.phantazm.zombies.map.handler.RoundHandler;
import org.phantazm.zombies.map.handler.ShopHandler;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.ZombiesPlayerModule;
import org.phantazm.zombies.sidebar.SidebarUpdater;

import java.util.*;
import java.util.function.Function;

public class InGameStage implements Stage {
    private final Instance instance;
    private final Collection<? extends ZombiesPlayer> zombiesPlayers;
    private final Map<UUID, SidebarUpdater> sidebarUpdaters = new HashMap<>();
    private final Pos spawnPos;
    private final RoundHandler roundHandler;
    private final Wrapper<Long> ticksSinceStart;
    private final Map<Key, List<Key>> defaultEquipment;
    private final Set<Key> equipmentGroups;
    private final Function<? super ZombiesPlayer, ? extends SidebarUpdater> sidebarUpdaterCreator;
    private final ShopHandler shopHandler;
    private final TickFormatter tickFormatter;

    private long startTime;

    public InGameStage(@NotNull Instance instance, @NotNull Collection<? extends ZombiesPlayer> zombiesPlayers,
            @NotNull Pos spawnPos, @NotNull RoundHandler roundHandler, @NotNull Wrapper<Long> ticksSinceStart,
            @NotNull Map<Key, List<Key>> defaultEquipment, @NotNull Set<Key> equipmentGroups,
            @NotNull Function<? super ZombiesPlayer, ? extends SidebarUpdater> sidebarUpdaterCreator,
            @NotNull ShopHandler shopHandler, @NotNull TickFormatter endTimeTickFormatter) {
        this.instance = Objects.requireNonNull(instance, "instance");
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
        this.spawnPos = Objects.requireNonNull(spawnPos, "spawnPos");
        this.roundHandler = Objects.requireNonNull(roundHandler, "roundHandler");
        this.ticksSinceStart = Objects.requireNonNull(ticksSinceStart, "ticksSinceStart");
        this.defaultEquipment = Objects.requireNonNull(defaultEquipment, "defaultEquipment");
        this.equipmentGroups = Objects.requireNonNull(equipmentGroups, "equipmentGroups");
        this.sidebarUpdaterCreator = Objects.requireNonNull(sidebarUpdaterCreator, "sidebarUpdaterCreator");
        this.shopHandler = Objects.requireNonNull(shopHandler, "shopHandler");
        this.tickFormatter = Objects.requireNonNull(endTimeTickFormatter, "tickFormatter");
    }

    @Override
    public boolean shouldContinue() {
        if (roundHandler.hasEnded()) {
            return true;
        }

        boolean anyAlive = false;
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            if (zombiesPlayer.isAlive()) {
                anyAlive = true;
                break;
            }
        }
        return !anyAlive;
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
        this.startTime = System.currentTimeMillis();

        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            zombiesPlayer.getPlayer().ifPresent(player -> {
                player.teleport(spawnPos);
            });

            ZombiesPlayerModule module = zombiesPlayer.module();
            EquipmentHandler equipmentHandler = module.getEquipmentHandler();
            for (Key groupKey : defaultEquipment.keySet()) {
                if (!equipmentHandler.canAddEquipment(groupKey)) {
                    continue;
                }

                for (Key key : defaultEquipment.get(groupKey)) {
                    module.getEquipmentCreator().createEquipment(key)
                            .ifPresent(equipment -> equipmentHandler.addEquipment(equipment, groupKey));

                    if (!equipmentHandler.canAddEquipment(groupKey)) {
                        break;
                    }
                }
            }

            for (Key group : equipmentGroups) {
                equipmentHandler.refreshGroup(group);
            }
        }

        shopHandler.initialize();
        ticksSinceStart.set(0L);
        roundHandler.setCurrentRound(0);
    }

    @Override
    public void tick(long time) {
        ticksSinceStart.apply(ticks -> ticks + 1);
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            if (!zombiesPlayer.hasQuit()) {
                SidebarUpdater sidebarUpdater = sidebarUpdaters.computeIfAbsent(zombiesPlayer.getUUID(), unused -> {
                    return sidebarUpdaterCreator.apply(zombiesPlayer);
                });
                sidebarUpdater.tick(time);
            }

            zombiesPlayer.getPlayer().ifPresent(player -> {
                for (ZombiesPlayer otherPlayer : zombiesPlayers) {
                    otherPlayer.module().getTabList().updateScore(player, zombiesPlayer.module().getKills().getKills());
                }
            });
        }
    }

    @Override
    public void end() {
        long endTime = System.currentTimeMillis();

        boolean anyAlive = false;
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            if (zombiesPlayer.isAlive()) {
                anyAlive = true;
                break;
            }
        }

        Component finalTime = tickFormatter.format((endTime - startTime) / MinecraftServer.TICK_MS);
        int bestRound = Math.min(roundHandler.currentRoundIndex() + 1, roundHandler.roundCount());

        if (anyAlive) {
            instance.sendTitlePart(TitlePart.TITLE, Component.text("You Win!", NamedTextColor.GREEN));
            instance.sendTitlePart(TitlePart.SUBTITLE, Component.text("You made it to Round ", NamedTextColor.GRAY)
                    .append(Component.text(bestRound, NamedTextColor.WHITE)).append(Component.text("!")));
        }
        else {
            instance.sendTitlePart(TitlePart.TITLE, Component.text("You lost...", NamedTextColor.RED));
        }

        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            if (zombiesPlayer.hasQuit()) {
                continue;
            }

            zombiesPlayer.sendMessage(Component.text("==========================================", NamedTextColor.GREEN,
                    TextDecoration.STRIKETHROUGH));

            zombiesPlayer.sendMessage(
                    Component.text("Zombies", NamedTextColor.YELLOW).append(Component.text(" - ", NamedTextColor.WHITE))
                            .append(finalTime).append(Component.text(" (", NamedTextColor.GRAY))
                            .append(Component.text("Round ", NamedTextColor.RED))
                            .append(Component.text(bestRound, NamedTextColor.RED))
                            .append(Component.text(")", NamedTextColor.GRAY)));

            zombiesPlayer.sendMessage(Component.empty());

            zombiesPlayer.sendMessage(
                    Component.text("Zombie Kills", Style.style(TextDecoration.BOLD, NamedTextColor.WHITE))
                            .append(Component.text(" - ", NamedTextColor.GRAY))
                            .append(Component.text(zombiesPlayer.module().getKills().getKills(),
                                    NamedTextColor.GREEN)));

            zombiesPlayer.sendMessage(Component.empty());

            zombiesPlayer.sendMessage(Component.text("==========================================",
                    NamedTextColor.GREEN, TextDecoration.STRIKETHROUGH));
        }

        for (SidebarUpdater sidebarUpdater : sidebarUpdaters.values()) {
            sidebarUpdater.end();
        }
    }

    @Override
    public @NotNull Key key() {
        return StageKeys.IN_GAME;
    }
}
