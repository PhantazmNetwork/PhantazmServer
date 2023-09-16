package org.phantazm.zombies.command;

import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.command.CommandUtils;
import org.phantazm.core.command.PermissionLockedCommand;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.scene2.SceneManager;
import org.phantazm.zombies.coin.PlayerCoins;
import org.phantazm.zombies.coin.TransactionResult;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class CoinsCommand extends PermissionLockedCommand {
    public static final Permission PERMISSION = new Permission("zombies.playtest.coins");
    private static final Argument<CoinAction> COIN_ACTION_ARGUMENT =
        ArgumentType.Enum("action", CoinAction.class).setFormat(ArgumentEnum.Format.LOWER_CASED);
    private static final Argument<Integer> COIN_AMOUNT_ARGUMENT = ArgumentType.Integer("amount");

    public CoinsCommand(@NotNull PlayerViewProvider provider) {
        super("coins", PERMISSION);

        addConditionalSyntax(CommandUtils.playerSenderCondition(), (sender, context) -> {
            PlayerView playerView = provider.fromPlayer((Player) sender);
            SceneManager.Global.instance().currentScene(playerView, ZombiesScene.class).ifPresent(scene -> {
                scene.getAcquirable().sync(self -> {
                    self.setLegit(false);

                    ZombiesPlayer zombiesPlayer = self.managedPlayers().get(playerView);
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
            });
        }, COIN_ACTION_ARGUMENT, COIN_AMOUNT_ARGUMENT);
    }

    public enum CoinAction {
        GIVE,
        TAKE,
        SET
    }
}
