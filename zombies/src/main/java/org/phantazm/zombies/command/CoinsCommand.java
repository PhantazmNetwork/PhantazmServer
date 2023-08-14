package org.phantazm.zombies.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.coin.PlayerCoins;
import org.phantazm.zombies.coin.TransactionResult;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class CoinsCommand extends Command {
    public static final Permission PERMISSION = new Permission("zombies.playtest.coins");

    public enum CoinAction {
        GIVE,
        TAKE,
        SET
    }

    private static final Argument<CoinAction> COIN_ACTION_ARGUMENT =
            ArgumentType.Enum("action", CoinAction.class).setFormat(ArgumentEnum.Format.LOWER_CASED);
    private static final Argument<Integer> COIN_AMOUNT_ARGUMENT = ArgumentType.Integer("amount");

    public CoinsCommand(@NotNull Function<? super UUID, Optional<ZombiesScene>> sceneMapper) {
        super("coins");
        Objects.requireNonNull(sceneMapper, "sceneMapper");

        setCondition((sender, commandString) -> sender.hasPermission(PERMISSION));
        addSyntax((sender, context) -> {
            UUID uuid = ((Player)sender).getUuid();
            sceneMapper.apply(uuid).ifPresent(scene -> {
                ZombiesPlayer zombiesPlayer = scene.getZombiesPlayers().get(uuid);
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
            });
        }, COIN_ACTION_ARGUMENT, COIN_AMOUNT_ARGUMENT);
    }
}
