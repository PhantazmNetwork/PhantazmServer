package org.phantazm.zombies.stage;

import com.github.steanky.toolkit.collection.Wrapper;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.EquipmentHandler;
import org.phantazm.zombies.map.handler.RoundHandler;
import org.phantazm.zombies.map.handler.ShopHandler;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.ZombiesPlayerModule;
import org.phantazm.zombies.sidebar.SidebarUpdater;

import java.util.*;
import java.util.function.Function;

public class InGameStage implements Stage {
    private final Collection<? extends ZombiesPlayer> zombiesPlayers;
    private final Map<UUID, SidebarUpdater> sidebarUpdaters = new HashMap<>();
    private final Pos spawnPos;
    private final RoundHandler roundHandler;
    private final Wrapper<Long> ticksSinceStart;
    private final Map<Key, List<Key>> defaultEquipment;
    private final Set<Key> equipmentGroups;
    private final Function<? super ZombiesPlayer, ? extends SidebarUpdater> sidebarUpdaterCreator;
    private final ShopHandler shopHandler;

    public InGameStage(@NotNull Collection<? extends ZombiesPlayer> zombiesPlayers, @NotNull Pos spawnPos,
            @NotNull RoundHandler roundHandler, @NotNull Wrapper<Long> ticksSinceStart,
            @NotNull Map<Key, List<Key>> defaultEquipment, @NotNull Set<Key> equipmentGroups,
            @NotNull Function<? super ZombiesPlayer, ? extends SidebarUpdater> sidebarUpdaterCreator,
            @NotNull ShopHandler shopHandler) {
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
        this.spawnPos = Objects.requireNonNull(spawnPos, "spawnPos");
        this.roundHandler = Objects.requireNonNull(roundHandler, "roundHandler");
        this.ticksSinceStart = Objects.requireNonNull(ticksSinceStart, "ticksSinceStart");
        this.defaultEquipment = Objects.requireNonNull(defaultEquipment, "defaultEquipment");
        this.equipmentGroups = Objects.requireNonNull(equipmentGroups, "equipmentGroups");
        this.sidebarUpdaterCreator = Objects.requireNonNull(sidebarUpdaterCreator, "sidebarUpdaterCreator");
        this.shopHandler = Objects.requireNonNull(shopHandler, "shopHandler");
    }

    public long ticksSinceStart() {
        return ticksSinceStart.get();
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
        zombiesPlayer.module().getMeta().setInGame(true);
    }

    @Override
    public void onLeave(@NotNull ZombiesPlayer zombiesPlayer) {

    }

    @Override
    public boolean hasPermanentPlayers() {
        return true;
    }

    @Override
    public boolean canRejoin() {
        return true;
    }

    @Override
    public void start() {
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            zombiesPlayer.module().getMeta().setInGame(true);
            zombiesPlayer.module().getStats().setGamesPlayed(zombiesPlayer.module().getStats().getGamesPlayed() + 1);
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
        for (SidebarUpdater sidebarUpdater : sidebarUpdaters.values()) {
            sidebarUpdater.end();
        }

        roundHandler.end();
    }

    @Override
    public @NotNull Key key() {
        return StageKeys.IN_GAME;
    }
}
