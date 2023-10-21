package org.phantazm.zombies.command;

import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.coin.PlayerCoins;
import org.phantazm.zombies.coin.TransactionResult;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

public class CoinsCommand extends SandboxLockedCommand {
    public static final Permission PERMISSION = new Permission("zombies.playtest.coins");
    private static final Argument<CoinAction> COIN_ACTION_ARGUMENT =
        ArgumentType.Enum("action", CoinAction.class).setFormat(ArgumentEnum.Format.LOWER_CASED);
    private static final Argument<Integer> COIN_AMOUNT_ARGUMENT = ArgumentType.Integer("amount");

    public CoinsCommand() {
        super("coins", PERMISSION, COIN_ACTION_ARGUMENT, COIN_AMOUNT_ARGUMENT);
    }

    @Override
    protected void runCommand(@NotNull CommandContext context, @NotNull ZombiesScene scene, @NotNull Player sender) {
        scene.setLegit(false);

        ZombiesPlayer zombiesPlayer = scene.managedPlayers().get(PlayerView.lookup(sender.getUuid()));
        if (zombiesPlayer == null) {
            return;
        }

        PlayerCoins playerCoins = zombiesPlayer.module().getCoins();
        int coinAmount = context.get(COIN_AMOUNT_ARGUMENT);

        switch (context.get(COIN_ACTION_ARGUMENT)) {
            case SET -> playerCoins.set(coinAmount);
            case GIVE -> {
                TransactionResult result = playerCoins.modify(coinAmount);
                playerCoins.applyTransaction(result);
            }
            case TAKE -> {
                TransactionResult result = playerCoins.modify(-coinAmount);
                playerCoins.applyTransaction(result);
            }
        }
    }

    public enum CoinAction {
        GIVE,
        TAKE,
        SET
    }
}
