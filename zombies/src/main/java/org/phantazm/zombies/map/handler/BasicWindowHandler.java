package org.phantazm.zombies.map.handler;

import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.tracker.BoundedTracker;
import org.phantazm.zombies.coin.ModifierSourceGroups;
import org.phantazm.zombies.coin.PlayerCoins;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.coin.TransactionResult;
import org.phantazm.zombies.event.ZombiesPlayerRepairWindowEvent;
import org.phantazm.zombies.map.Window;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.stage.StageKeys;

import java.util.*;

public class BasicWindowHandler implements WindowHandler {
    private static final int POSITION_CHECK_INTERVAL = 200;
    private static final int UNREPAIRABLE_BREAK_DELAY = 2000;

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

    private final WindowMessages windowMessages;

    private long lastPositionCheck;

    public BasicWindowHandler(@NotNull BoundedTracker<Window> windowTracker,
            @NotNull Collection<? extends ZombiesPlayer> players, double repairRadius, long repairInterval,
            int coinsPerWindowBlock, @NotNull WindowMessages windowMessages) {
        this.windowTracker = Objects.requireNonNull(windowTracker, "windowTracker");
        this.players = Objects.requireNonNull(players, "players");
        this.repairRadius = repairRadius;
        this.repairInterval = repairInterval;
        this.repairOperationMap = new LinkedHashMap<>();
        this.activeRepairs = repairOperationMap.values();
        this.coinsPerWindowBlock = coinsPerWindowBlock;
        this.lastPositionCheck = System.currentTimeMillis();
        this.windowMessages = Objects.requireNonNull(windowMessages, "windowMessages");
    }

    @Override
    public void handleCrouchStateChange(@NotNull ZombiesPlayer zombiesPlayer, boolean crouching) {
        Optional<Player> playerOptional = zombiesPlayer.getPlayer();

        if (!crouching && playerOptional.isPresent() && zombiesPlayer.canRepairWindow() &&
                zombiesPlayer.inStage(StageKeys.IN_GAME)) {
            RepairOperation repairOperation = repairOperationMap.remove(zombiesPlayer.getUUID());
            if (repairOperation != null && !repairOperation.window.isFullyRepaired()) {
                zombiesPlayer.sendMessage(windowMessages.stopRepairing());
            }

            return;
        }

        if (!crouching || playerOptional.isEmpty() || !zombiesPlayer.canRepairWindow() ||
                !zombiesPlayer.inStage(StageKeys.IN_GAME)) {
            repairOperationMap.remove(zombiesPlayer.getUUID());
            return;
        }

        Player player = playerOptional.get();
        addOperationIfNearby(zombiesPlayer, player);
    }

    private void addOperationIfNearby(ZombiesPlayer zombiesPlayer, Player player) {
        BoundingBox boundingBox = player.getBoundingBox();
        double width = boundingBox.width();
        double height = boundingBox.height();

        windowTracker.closestInRangeToBounds(player.getPosition(), width, height, repairRadius).ifPresent(window -> {
            repairOperationMap.computeIfAbsent(player.getUuid(), ignored -> {
                if (!window.isFullyRepaired()) {
                    player.sendMessage(windowMessages.startRepairing());
                }

                return new RepairOperation(zombiesPlayer, window, System.currentTimeMillis());
            });
        });
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

                    if (player.isSneaking()) {
                        addOperationIfNearby(zombiesPlayer, player);
                    }
                    else {
                        BoundingBox boundingBox = player.getBoundingBox();
                        double width = boundingBox.width();
                        double height = boundingBox.height();

                        Optional<Window> windowOptional =
                                windowTracker.closestInRangeToBounds(player.getPosition(), width, height, repairRadius);
                        if (windowOptional.isPresent()) {
                            if (!windowOptional.get().isFullyRepaired()) {
                                player.sendActionBar(windowMessages.nearWindow());
                            }
                        }
                    }
                }
            }

            lastPositionCheck = time;
        }

        Iterator<RepairOperation> repairOperationIterator = activeRepairs.iterator();
        while (repairOperationIterator.hasNext()) {
            RepairOperation repairOperation = repairOperationIterator.next();
            ZombiesPlayer zombiesPlayer = repairOperation.zombiesPlayer;
            if (!zombiesPlayer.canRepairWindow()) {
                repairOperationIterator.remove();
                continue;
            }

            {
                Optional<Player> playerOptional = zombiesPlayer.getPlayer();
                if (playerOptional.isEmpty()) {
                    repairOperationIterator.remove();
                    continue;
                }

                Player player = playerOptional.get();
                Window targetWindow = repairOperation.window;

                BoundingBox boundingBox = player.getBoundingBox();
                double width = boundingBox.width();
                double height = boundingBox.height();

                Optional<Window> windowOptional =
                        windowTracker.closestInRangeToBounds(player.getPosition(), width, height, repairRadius);
                if (windowOptional.isEmpty() || windowOptional.get() != targetWindow) {
                    repairOperationIterator.remove();
                    continue;
                }
            }

            long elapsedMS = time - repairOperation.lastRepairTime;
            long elapsedTicks = elapsedMS / MinecraftServer.TICK_MS;

            if (elapsedTicks >= repairInterval) {
                repairOperation.lastRepairTime = time;

                Window targetWindow = repairOperation.window;
                if (!targetWindow.isFullyRepaired()) {
                    long timeElapsedSinceLastBroken = time - targetWindow.getLastBreakTime();
                    if (timeElapsedSinceLastBroken < UNREPAIRABLE_BREAK_DELAY) {
                        zombiesPlayer.sendMessage(windowMessages.enemiesNearby());
                        continue;
                    }

                    int repaired = targetWindow.updateIndex(
                            targetWindow.getIndex() + zombiesPlayer.module().getMeta().getWindowRepairAmount());
                    if (repaired == 0) {
                        continue;
                    }

                    Optional<Player> playerOptional = zombiesPlayer.getPlayer();
                    if (playerOptional.isEmpty()) {
                        repairOperationIterator.remove();
                        continue;
                    }

                    Player player = playerOptional.get();

                    int baseGold = repaired * coinsPerWindowBlock;
                    PlayerCoins coins = zombiesPlayer.module().getCoins();

                    TransactionResult result = coins.runTransaction(new Transaction(
                            zombiesPlayer.module().compositeTransactionModifiers()
                                    .modifiers(ModifierSourceGroups.WINDOW_COIN_GAIN), baseGold));

                    ZombiesPlayerRepairWindowEvent event =
                            new ZombiesPlayerRepairWindowEvent(player, zombiesPlayer, targetWindow, repaired,
                                    result.change());

                    EventDispatcher.call(event);
                    if (event.isCancelled()) {
                        continue;
                    }

                    coins.applyTransaction(new TransactionResult(result.displays(), event.goldGain()));
                    if (targetWindow.isFullyRepaired()) {
                        zombiesPlayer.sendMessage(windowMessages.finishRepairing());
                    }
                }
            }
        }
    }
}
