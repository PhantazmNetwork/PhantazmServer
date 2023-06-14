package org.phantazm.zombies.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.Flags;
import org.phantazm.zombies.map.Flaggable;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class GodmodeCommand extends Command {
    public GodmodeCommand(@NotNull Function<? super UUID, ? extends Optional<ZombiesScene>> sceneMapper) {
        super("godmode");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("You have to be a player to use that command!", NamedTextColor.RED));
                return;
            }

            UUID uuid = player.getUuid();
            sceneMapper.apply(uuid).ifPresent(scene -> {
                ZombiesPlayer zombiesPlayer = scene.getZombiesPlayers().get(uuid);
                Flaggable flags = zombiesPlayer.module().flags();

                boolean res = flags.toggleFlag(Flags.GODMODE);
                if (res) {
                    player.setAllowFlying(true);
                    player.sendMessage("Enabled godmode.");
                }
                else {
                    player.setAllowFlying(false);
                    player.setFlying(false);
                    player.sendMessage("Disabled godmode.");
                }
            });
        });
    }
}
