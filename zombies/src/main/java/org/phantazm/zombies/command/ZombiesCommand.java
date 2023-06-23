package org.phantazm.zombies.command;

import com.github.steanky.element.core.key.KeyParser;
import net.kyori.adventure.key.Key;
import net.minestom.server.command.builder.Command;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.Scene;
import org.phantazm.core.game.scene.command.QuitCommand;
import org.phantazm.core.game.scene.fallback.SceneFallback;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.zombies.map.MapInfo;
import org.phantazm.zombies.scene.ZombiesJoinHelper;
import org.phantazm.zombies.scene.ZombiesSceneRouter;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class ZombiesCommand extends Command {
    public ZombiesCommand(@NotNull Map<? super UUID, ? extends Party> parties, @NotNull ZombiesSceneRouter router,
            @NotNull KeyParser keyParser, @NotNull Map<Key, MapInfo> maps, @NotNull PlayerViewProvider viewProvider,
            @NotNull Function<? super UUID, Optional<? extends Scene<?>>> sceneMapper,
            @NotNull SceneFallback fallback) {
        super("zombies");

        Objects.requireNonNull(router, "router");
        Objects.requireNonNull(sceneMapper, "sceneMapper");
        Objects.requireNonNull(keyParser, "keyParser");
        Objects.requireNonNull(maps, "maps");
        Objects.requireNonNull(viewProvider, "viewProvider");
        Objects.requireNonNull(fallback, "fallback");

        ZombiesJoinHelper joinHelper = new ZombiesJoinHelper(parties, viewProvider, router, sceneMapper);
        addSubcommand(new ZombiesJoinCommand(keyParser, maps, joinHelper));
        addSubcommand(new CoinsCommand(router::getScene));
        addSubcommand(new RoundCommand(router::getScene));
        addSubcommand(new KillAllCommand(router::getScene));
        addSubcommand(new GodmodeCommand(router::getScene));
        addSubcommand(new AmmoRefillCommand(router::getScene));
        addSubcommand(new FlagToggleCommand(router::getScene, keyParser));
        addSubcommand(new QuitCommand(sceneMapper, fallback, viewProvider));
        addSubcommand(new ZombiesRejoinCommand(router, joinHelper));
    }
}
