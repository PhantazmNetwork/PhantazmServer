package org.phantazm.zombies.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.zombies.Tags;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class KillAllCommand extends Command {
    public static final Permission PERMISSION = new Permission("zombies.playtest.killall");

    public KillAllCommand(@NotNull Function<? super UUID, Optional<ZombiesScene>> sceneMapper) {
        super("killall");
        Objects.requireNonNull(sceneMapper, "sceneMapper");

        setCondition((sender, commandString) -> sender.hasPermission(PERMISSION));
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("You have to be a player to use that command!", NamedTextColor.RED));
                return;
            }

            UUID uuid = player.getUuid();
            sceneMapper.apply(uuid).flatMap(scene -> scene.getMap().roundHandler().currentRound()).ifPresent(round -> {
                for (PhantazmMob mob : round.getSpawnedMobs()) {
                    mob.entity().setTag(Tags.LAST_HIT_BY, uuid);
                    mob.entity().kill();
                }
            });
        });
    }
}
