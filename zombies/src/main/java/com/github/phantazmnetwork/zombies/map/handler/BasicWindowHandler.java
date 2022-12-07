package com.github.phantazmnetwork.zombies.map.handler;

import com.github.phantazmnetwork.zombies.coin.*;
import com.github.phantazmnetwork.zombies.map.Window;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BasicWindowHandler implements WindowHandler {
    private static class RepairOperation {
        private final ZombiesPlayer zombiesPlayer;
        private final Window window;
        private long lastRepairTime;

        private RepairOperation(ZombiesPlayer zombiesPlayer, Window window, long lastRepairTime) {
            this.zombiesPlayer = zombiesPlayer;
            this.window = window;
            this.lastRepairTime = lastRepairTime;
        }
    }

    private final List<Window> windows;
    private final double repairRadius;
    private final long repairInterval;
    private final int coinsPerWindowBlock;

    private final Map<UUID, RepairOperation> repairOperationMap;
    private final Collection<RepairOperation> activeRepairs;

    public BasicWindowHandler(@NotNull List<Window> windows, double repairRadius, long repairInterval,
            int coinsPerWindowBlock) {
        this.windows = Objects.requireNonNull(windows, "windows");
        this.repairRadius = repairRadius;
        this.repairInterval = repairInterval;
        this.repairOperationMap = new LinkedHashMap<>();
        this.activeRepairs = repairOperationMap.values();
        this.coinsPerWindowBlock = coinsPerWindowBlock;
    }

    @Override
    public void handleCrouchStateChange(@NotNull ZombiesPlayer zombiesPlayer, boolean crouching) {
        Optional<Player> playerOptional = zombiesPlayer.getPlayer();

        if (!crouching || !zombiesPlayer.isAlive() || playerOptional.isEmpty()) {
            repairOperationMap.remove(zombiesPlayer.getUUID());
        }
        else {
            Player player = playerOptional.get();

            for (Window window : windows) {
                if (window.isInRange(player.getPosition(), repairRadius)) {
                    repairOperationMap.put(player.getUuid(),
                            new RepairOperation(zombiesPlayer, window, System.currentTimeMillis()));
                    return;
                }
            }
        }
    }

    @Override
    public void tick(long time) {
        Iterator<RepairOperation> repairOperationIterator = activeRepairs.iterator();
        while (repairOperationIterator.hasNext()) {
            RepairOperation repairOperation = repairOperationIterator.next();
            ZombiesPlayer zombiesPlayer = repairOperation.zombiesPlayer;
            if (!zombiesPlayer.isAlive()) {
                repairOperationIterator.remove();
                continue;
            }

            long elapsedMS = time - repairOperation.lastRepairTime;
            long elapsedTicks = elapsedMS / MinecraftServer.TICK_MS;

            if (elapsedTicks >= repairInterval) {
                Window targetWindow = repairOperation.window;
                if (!targetWindow.isFullyRepaired()) {
                    int repaired = targetWindow.updateIndex(
                            targetWindow.getIndex() + zombiesPlayer.getModule().getMeta().getWindowRepairAmount());

                    int baseGold = repaired * coinsPerWindowBlock;
                    PlayerCoins coins = zombiesPlayer.getModule().getCoins();

                    TransactionResult result = coins.runTransaction(new Transaction(
                            zombiesPlayer.getModule().compositeTransactionModifiers()
                                    .modifiers(ModifierSourceGroups.WINDOW_COIN_GAIN), baseGold));

                    coins.applyTransaction(result);
                }

                repairOperation.lastRepairTime = time;
            }
        }
    }
}
