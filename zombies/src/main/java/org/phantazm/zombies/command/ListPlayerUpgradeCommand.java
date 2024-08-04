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

import java.util.Map;
import java.util.Objects;

public class ListPlayerUpgradeCommand extends SandboxLockedCommand {
    public static final Permission PERMISSION = new Permission("zombies.playtest.list_player_upgrade");

    public ListPlayerUpgradeCommand() {
        super("upgrade_list", PERMISSION);
    }

    @Override
    protected void runCommand(@NotNull CommandContext context, @NotNull ZombiesScene scene, @NotNull Player sender) {
        @Subst(Constants.NAMESPACE_OR_KEY)
        PlayerUpgradeHandler handler = scene.upgradeHandler(sender.getUuid());
        if (handler == null) {
            sender.sendMessage("No upgrade handler for " + sender.getUsername() + " (are upgrades enabled for this map?)");
            return;
        }

        for (Map.Entry<Key, PlayerUpgrade> entry : handler.activeUpgrades()) {
            sender.sendMessage(Component.text(entry.getKey().asString(), NamedTextColor.GRAY));
        }
    }
}
