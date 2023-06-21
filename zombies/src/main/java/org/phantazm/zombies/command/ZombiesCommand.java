package org.phantazm.zombies.command;

import com.github.steanky.element.core.key.KeyParser;
import net.kyori.adventure.key.Key;
import net.minestom.server.command.builder.Command;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.Scene;
import org.phantazm.core.game.scene.SceneRouter;
import org.phantazm.core.game.scene.fallback.SceneFallback;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.zombies.map.MapInfo;
import org.phantazm.zombies.scene.ZombiesScene;
import org.phantazm.zombies.scene.ZombiesSceneRouter;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class ZombiesCommand extends Command {
    public ZombiesCommand(@NotNull Map<? super UUID, ? extends Party> parties, @NotNull ZombiesSceneRouter router,
            @NotNull KeyParser keyParser, @NotNull Map<Key, MapInfo> maps, @NotNull PlayerViewProvider viewProvider,
            @NotNull Function<? super UUID, ? extends Optional<ZombiesScene>> sceneMapper,
            @NotNull Function<? super UUID, Optional<SceneRouter<? extends Scene<?>, ?>>> globalRouterMapper,
            @NotNull SceneFallback fallback) {
        super("zombies");

        Objects.requireNonNull(router, "router");
        Objects.requireNonNull(globalRouterMapper, "globalRouterMapper");
        Objects.requireNonNull(keyParser, "keyParser");
        Objects.requireNonNull(maps, "maps");
        Objects.requireNonNull(viewProvider, "viewProvider");
        Objects.requireNonNull(fallback, "fallback");

        addSubcommand(new ZombiesJoinCommand(router, globalRouterMapper, keyParser, maps, viewProvider, parties));
        addSubcommand(new CoinsCommand(sceneMapper));
        addSubcommand(new RoundCommand(sceneMapper));
        addSubcommand(new KillAllCommand(sceneMapper));
        addSubcommand(new GodmodeCommand(sceneMapper));
        addSubcommand(new AmmoRefillCommand(sceneMapper));
        addSubcommand(new FlagToggleCommand(sceneMapper, keyParser));
        addSubcommand(new QuitCommand(router, fallback, viewProvider));
        addSubcommand(new ZombiesRejoinCommand(router, globalRouterMapper, viewProvider, parties));
    }
}
