package org.phantazm.zombies.command;

import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.PlayerUpgradeHandler;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Objects;

public class TogglePlayerUpgradeCommand extends SandboxLockedCommand {
    public static final Permission PERMISSION = new Permission("zombies.playtest.toggle_player_upgrade");

    private final KeyParser keyParser;

    public TogglePlayerUpgradeCommand(@NotNull KeyParser keyParser) {
        super("upgrade", PERMISSION, ArgumentType.String("player-upgrade"));
        this.keyParser = Objects.requireNonNull(keyParser);
    }

    @Override
    protected void runCommand(@NotNull CommandContext context, @NotNull ZombiesScene scene, @NotNull Player sender) {
        @Subst(Constants.NAMESPACE_OR_KEY)
        String upgrade = context.get("player-upgrade");
        if (!keyParser.isValidKey(upgrade)) {
            return;
        }

        PlayerUpgradeHandler handler = scene.upgradeHandler(sender.getUuid());
        if (handler == null) {
            return;
        }

        Key key = keyParser.parseKey(upgrade);
        if (!handler.hasUpgrade(key)) {
            sender.sendMessage(Component.text("Invalid upgrade " + key, NamedTextColor.RED));
            return;
        }

        PlayerUpgrade playerUpgrade = handler.getUpgrade(key);

        if (playerUpgrade != null && playerUpgrade.isActivated()) {
            handler.deactivateUpgrade(key);
            sender.sendMessage("Deactivated " + key);
        } else {
            handler.activateUpgrade(key);
            sender.sendMessage("Activated " + key);
        }
    }
}
