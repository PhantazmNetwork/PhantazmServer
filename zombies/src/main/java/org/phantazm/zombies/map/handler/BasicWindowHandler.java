package org.phantazm.zombies.map.handler;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.tracker.BoundedTracker;
import org.phantazm.zombies.coin.ModifierSourceGroups;
import org.phantazm.zombies.coin.PlayerCoins;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.coin.TransactionResult;
import org.phantazm.zombies.event.PlayerRepairWindowEvent;
import org.phantazm.zombies.map.Window;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.stage.StageKeys;

import java.util.*;

public class BasicWindowHandler implements WindowHandler {
    private static final int POSITION_CHECK_INTERVAL = 200;
    private static final int UNREPAIRABLE_BREAK_DELAY = 1000;

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
        if (!zombiesPlayer.inStage(StageKeys.IN_GAME)) {
            return;
        }

        Optional<Player> playerOptional = zombiesPlayer.getPlayer();

        if (!crouching || playerOptional.isEmpty() || !zombiesPlayer.canRepairWindow()) {
            repairOperationMap.remove(zombiesPlayer.getUUID());
            return;
        }

        Player player = playerOptional.get();
        addOperationIfNearby(zombiesPlayer, player);
    }

    private void addOperationIfNearby(ZombiesPlayer zombiesPlayer, Player player) {
        windowTracker.closestInRangeToCenter(player.getPosition(), repairRadius).ifPresent(
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
                    long timeElapsedSinceLastBroken = time - targetWindow.getLastBreakTime();
                    if (timeElapsedSinceLastBroken < UNREPAIRABLE_BREAK_DELAY) {
                        zombiesPlayer.sendMessage(Component.text("You can't repair windows while enemies are nearby!")
                                .color(NamedTextColor.RED));
                        repairOperation.lastRepairTime = time;
                        return;
                    }

                    int repaired = targetWindow.updateIndex(
                            targetWindow.getIndex() + zombiesPlayer.module().getMeta().getWindowRepairAmount());

                    int baseGold = repaired * coinsPerWindowBlock;
                    PlayerCoins coins = zombiesPlayer.module().getCoins();

                    TransactionResult result = coins.runTransaction(new Transaction(
                            zombiesPlayer.module().compositeTransactionModifiers()
                                    .modifiers(ModifierSourceGroups.WINDOW_COIN_GAIN), baseGold));

                    coins.applyTransaction(result);

                    if (repaired != 0) {
                        zombiesPlayer.getPlayer().ifPresent(player -> {
                            EventDispatcher.call(
                                    new PlayerRepairWindowEvent(player, zombiesPlayer, targetWindow, repaired));
                        });
                    }
                }

                repairOperation.lastRepairTime = time;
            }
        }
    }
}
