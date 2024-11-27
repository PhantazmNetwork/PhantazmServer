package org.phantazm.zombies.command;

import com.github.steanky.element.core.key.Constants;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.CommandUtils;
import org.phantazm.loader.Loader;
import org.phantazm.zombies.powerup.PowerupHandler;
import org.phantazm.zombies.powerup.PowerupUtils;
import org.phantazm.zombies.scene2.ZombiesScene;
import org.phantazm.zombies.stage.Stage;
import org.phantazm.zombies.stage.StageKeys;

import java.util.Objects;

public class SpawnPowerupCommand extends SandboxLockedCommand {
    public static final Permission PERMISSION = new Permission("zombies.playtest.spawn_powerup");

    public static final Argument<Integer> ROUND = ArgumentType.Integer("round").setDefaultValue(-1);

    public SpawnPowerupCommand(Loader<PowerupHandler.Source> powerupLoader) {
        super("spawn_powerup", PERMISSION, ArgumentType.Word("powerup-identifier")
            .setSuggestionCallback((sender, context, suggestion) -> {
                CommandUtils.tabComplete(suggestion, powerupLoader.first().powerups().entrySet(), powerupEntry -> {
                    return powerupEntry.getKey().asString();
                });
            }), ROUND);
    }

    @Override
    protected void runCommand(@NotNull CommandContext context, @NotNull ZombiesScene scene, @NotNull Player sender) {
        scene.setLegit(false);

        Stage stage = scene.currentStage();
        if (stage == null || !Objects.equals(stage.key(), StageKeys.IN_GAME)) {
            sender.sendMessage(Component.text("Game has not started yet!", NamedTextColor.RED));
            return;
        }

        @Subst(Constants.NAMESPACE_OR_KEY) String powerup = context.get("powerup-identifier");
        if (!Key.parseable(powerup)) {
            sender.sendMessage(Component.text("Bad powerup!", NamedTextColor.RED));
            return;
        }

        PowerupHandler powerupHandler = scene.map().powerupHandler();
        Key powerupKey = Key.key(powerup);
        if (!powerupHandler.canSpawnType(powerupKey)) {
            sender.sendMessage(Component.text("Bad powerup!", NamedTextColor.RED));
            return;
        }

        Point position = PowerupUtils.powerupSpawnPosition(scene.instance(),
            sender.getPosition());
        powerupHandler.spawn(powerupKey, position);
        sender.sendMessage(Component.text("Spawned powerup at " + position, NamedTextColor.GREEN));
    }
}
