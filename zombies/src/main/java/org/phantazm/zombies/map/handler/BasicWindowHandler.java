package org.phantazm.zombies.map.handler;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.tracker.BoundedTracker;
import org.phantazm.zombies.coin.ModifierSourceGroups;
import org.phantazm.zombies.coin.PlayerCoins;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.coin.TransactionResult;
import org.phantazm.zombies.map.Window;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.*;

public class BasicWindowHandler implements WindowHandler {
    private static final int POSITION_CHECK_INTERVAL = 200;

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

    private final BoundedTracker<Window> windowTracker;
    private final Collection<? extends ZombiesPlayer> players;
    private final double repairRadius;
    private final long repairInterval;
    private final int coinsPerWindowBlock;

    private final Map<UUID, RepairOperation> repairOperationMap;
    private final Collection<RepairOperation> activeRepairs;

    private long lastPositionCheck;

    public BasicWindowHandler(@NotNull BoundedTracker<Window> windowTracker,
            @NotNull Collection<? extends ZombiesPlayer> players, double repairRadius, long repairInterval,
            int coinsPerWindowBlock) {
        this.windowTracker = Objects.requireNonNull(windowTracker, "windowTracker");
        this.players = Objects.requireNonNull(players, "players");
        this.repairRadius = repairRadius;
        this.repairInterval = repairInterval;
        this.repairOperationMap = new LinkedHashMap<>();
        this.activeRepairs = repairOperationMap.values();
        this.coinsPerWindowBlock = coinsPerWindowBlock;
        this.lastPositionCheck = System.currentTimeMillis();
    }

    @Override
    public void handleCrouchStateChange(@NotNull ZombiesPlayer zombiesPlayer, boolean crouching) {
        Optional<Player> playerOptional = zombiesPlayer.getPlayer();

        if (!crouching || playerOptional.isEmpty() || !zombiesPlayer.canRepairWindow()) {
            repairOperationMap.remove(zombiesPlayer.getUUID());
            return;
        }

        Player player = playerOptional.get();
        addOperationIfNearby(zombiesPlayer, player);
    }

    private void addOperationIfNearby(ZombiesPlayer zombiesPlayer, Player player) {
        windowTracker.closestInRange(player.getPosition(), repairRadius).ifPresent(
                window -> repairOperationMap.putIfAbsent(player.getUuid(),
                        new RepairOperation(zombiesPlayer, window, System.currentTimeMillis())));
    }

    @Override
    public @NotNull BoundedTracker<Window> tracker() {
        return windowTracker;
    }

    @Override
    public void tick(long time) {
        if (time - lastPositionCheck >= POSITION_CHECK_INTERVAL) {
            for (ZombiesPlayer zombiesPlayer : players) {
                Optional<Player> playerOptional = zombiesPlayer.getPlayer();
                if (playerOptional.isPresent() && zombiesPlayer.canRepairWindow()) {
                    Player player = playerOptional.get();

                    if (player.getPose() == Entity.Pose.SNEAKING) {
                        addOperationIfNearby(zombiesPlayer, player);
                    }
                }
            }

            lastPositionCheck = time;
        }

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
