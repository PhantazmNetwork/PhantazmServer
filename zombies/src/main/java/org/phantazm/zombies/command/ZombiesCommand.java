package org.phantazm.zombies.command;

import com.github.steanky.element.core.key.KeyParser;
import net.kyori.adventure.key.Key;
import net.minestom.server.command.builder.Command;
import net.minestom.server.timer.SchedulerManager;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.SceneTransferHelper;
import org.phantazm.core.game.scene.fallback.SceneFallback;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.zombies.map.MapInfo;
import org.phantazm.zombies.scene.ZombiesJoinHelper;
import org.phantazm.zombies.scene.ZombiesSceneRouter;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ZombiesCommand extends Command {
    public ZombiesCommand(@NotNull Map<? super UUID, ? extends Party> parties, @NotNull ZombiesSceneRouter router,
            @NotNull KeyParser keyParser, @NotNull Map<Key, MapInfo> maps, @NotNull PlayerViewProvider viewProvider,
            @NotNull SchedulerManager schedulerManager, @NotNull SceneTransferHelper transferHelper,
            @NotNull SceneFallback fallback, long joinRatelimit) {
        super("zombies");

        Objects.requireNonNull(parties, "parties");
        Objects.requireNonNull(router, "router");
        Objects.requireNonNull(keyParser, "keyParser");
        Objects.requireNonNull(maps, "maps");
        Objects.requireNonNull(viewProvider, "viewProvider");
        Objects.requireNonNull(fallback, "fallback");

        ZombiesJoinHelper joinHelper = new ZombiesJoinHelper(viewProvider, router, schedulerManager, transferHelper);
        addSubcommand(new ZombiesJoinCommand(parties, viewProvider, keyParser, maps, joinHelper,
                joinRatelimit));
        addSubcommand(new CoinsCommand(router::getCurrentScene));
        addSubcommand(new RoundCommand(router::getCurrentScene, schedulerManager));
        addSubcommand(new KillAllCommand(router::getCurrentScene));
        addSubcommand(new GodmodeCommand(router::getCurrentScene));
        addSubcommand(new AmmoRefillCommand(router::getCurrentScene));
        addSubcommand(new FlagToggleCommand(router::getCurrentScene, keyParser));
        addSubcommand(new ZombiesRejoinCommand(router, viewProvider, joinHelper));
    }
}
