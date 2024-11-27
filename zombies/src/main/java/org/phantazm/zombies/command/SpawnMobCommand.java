package org.phantazm.zombies.command;

import com.github.steanky.element.core.key.Constants;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.CommandUtils;
import org.phantazm.loader.Loader;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.MobCreator;
import org.phantazm.mob2.MobMeta;
import org.phantazm.mob2.MobSpawner;
import org.phantazm.zombies.map.handler.RoundHandler;
import org.phantazm.zombies.scene2.ZombiesScene;
import org.phantazm.zombies.stage.Stage;
import org.phantazm.zombies.stage.StageKeys;

import java.util.Objects;

public class SpawnMobCommand extends SandboxLockedCommand {
    public static final Permission PERMISSION = new Permission("zombies.playtest.spawnmob");

    public static final Argument<Integer> ROUND = ArgumentType.Integer("round").setDefaultValue(-1);

    public SpawnMobCommand(Loader<MobCreator> mobLoader) {
        super("spawnmob", PERMISSION, ArgumentType.Word("mob-identifier")
            .setSuggestionCallback((sender, context, suggestion) -> {
                CommandUtils.tabComplete(suggestion, mobLoader.data().entrySet(), mobEntry -> {
                    return mobEntry.getKey().asString();
                }, null, mobEntry -> {
                    MobMeta meta = mobEntry.getValue().data().meta();
                    if (meta == null) return null;

                    return meta.customName();
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

        @Subst(Constants.NAMESPACE_OR_KEY) String mob = context.get("mob-identifier");
        if (!Key.parseable(mob)) {
            sender.sendMessage(Component.text("Bad mob!", NamedTextColor.RED));
            return;
        }

        MobSpawner spawner = scene.map().objects().mobSpawner();
        Key mobKey = Key.key(mob);
        if (!spawner.canSpawn(mobKey)) {
            sender.sendMessage(Component.text("Bad mob!", NamedTextColor.RED));
            return;
        }

        Mob spawned = spawner.spawn(mobKey, scene.instance(), sender.getPosition());

        sender.sendMessage(Component.text("Spawned " + mob, NamedTextColor.GREEN));

        RoundHandler roundHandler = scene.map().roundHandler();
        roundHandler.endless().ifPresent(endless -> {
            int round = context.get(ROUND);

            int roundIndex = round - 1;
            if (roundIndex < 0) {
                return;
            }

            int endlessRound = (roundIndex - roundHandler.roundCount()) + 1;
            if (endlessRound < 0) {
                return;
            }

            spawned.getAcquirable().sync(self -> {
                endless.scaleMob((Mob) self, endlessRound);
            });

            sender.sendMessage(Component.text("Mob stats scaled to round " + round, NamedTextColor.GREEN));
        });

        roundHandler.currentRound().ifPresent(round -> {
            round.addMob(spawned);
        });
    }
}
