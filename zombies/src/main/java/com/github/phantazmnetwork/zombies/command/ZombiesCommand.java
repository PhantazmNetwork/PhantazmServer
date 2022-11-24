package com.github.phantazmnetwork.zombies.command;

import com.github.phantazmnetwork.core.game.scene.Scene;
import com.github.phantazmnetwork.core.player.PlayerViewProvider;
import com.github.phantazmnetwork.zombies.map.MapInfo;
import com.github.phantazmnetwork.zombies.scene.ZombiesRouteRequest;
import com.github.steanky.element.core.key.KeyParser;
import net.kyori.adventure.key.Key;
import net.minestom.server.command.builder.Command;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class ZombiesCommand extends Command {

    public ZombiesCommand(@NotNull Scene<ZombiesRouteRequest> router, @NotNull KeyParser keyParser,
            @NotNull Map<Key, MapInfo> maps, @NotNull PlayerViewProvider viewProvider) {
        super("zombies");

        Objects.requireNonNull(router, "router");
        Objects.requireNonNull(keyParser, "keyParser");
        Objects.requireNonNull(maps, "maps");
        Objects.requireNonNull(viewProvider, "viewProvider");

        addSubcommand(new ZombiesJoinCommand(router, keyParser, maps, viewProvider));
    }

}
