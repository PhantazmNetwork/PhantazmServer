package org.phantazm.zombies.command;

import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.command.CommandUtils;
import org.phantazm.core.command.PermissionLockedCommand;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.zombies.Tags;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class KillAllCommand extends PermissionLockedCommand {
    public static final Permission PERMISSION = new Permission("zombies.playtest.killall");

    public KillAllCommand(@NotNull Function<? super UUID, Optional<ZombiesScene>> sceneMapper) {
        super("killall", PERMISSION);
        Objects.requireNonNull(sceneMapper, "sceneMapper");

        addConditionalSyntax(CommandUtils.playerSenderCondition(), (sender, context) -> {
            Player player = (Player)sender;
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
