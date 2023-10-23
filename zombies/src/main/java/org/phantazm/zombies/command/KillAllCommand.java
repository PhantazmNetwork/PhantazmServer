package org.phantazm.zombies.command;

import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.scene2.ZombiesScene;

public class KillAllCommand extends SandboxLockedCommand {
    public static final Permission PERMISSION = new Permission("zombies.playtest.killall");

    public KillAllCommand() {
        super("killall", PERMISSION);
    }

    @Override
    protected void runCommand(@NotNull CommandContext context, @NotNull ZombiesScene scene, @NotNull Player sender) {
        scene.setLegit(false);

        scene.map().roundHandler().currentRound().ifPresent(round -> {
            for (Mob mob : round.getSpawnedMobs()) {
                mob.getAcquirable().sync(self1 -> {
                    ((LivingEntity) self1).damage(Damage.fromPlayer(sender, mob.getHealth()), true);
                });
            }
        });
    }
}
