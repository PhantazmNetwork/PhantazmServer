package org.phantazm.zombies.command;

import com.github.steanky.element.core.key.KeyParser;
import net.kyori.adventure.key.Key;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.stats.zombies.ZombiesDatabase;
import org.phantazm.zombies.map.MapInfo;
import org.phantazm.zombies.scene2.ZombiesJoiner;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

public class ZombiesCommand extends Command {
    public ZombiesCommand(@NotNull ZombiesJoiner joiner, @NotNull Map<? super UUID, ? extends Party> parties,
        @NotNull KeyParser keyParser, @NotNull Map<Key, MapInfo> maps, @NotNull PlayerViewProvider viewProvider,
        long joinRatelimit, @NotNull ZombiesDatabase zombiesDatabase) {
        super("zombies");

        Objects.requireNonNull(joiner);
        Objects.requireNonNull(parties);
        Objects.requireNonNull(keyParser);
        Objects.requireNonNull(maps);
        Objects.requireNonNull(viewProvider);

        addSubcommand(new ZombiesJoinCommand(joiner, parties, viewProvider, keyParser, maps, joinRatelimit,
            zombiesDatabase));
        addSubcommand(new CoinsCommand(viewProvider));
        addSubcommand(new RoundCommand(viewProvider));
        addSubcommand(new KillAllCommand(viewProvider));
        addSubcommand(new GodmodeCommand(viewProvider));
        addSubcommand(new AmmoRefillCommand(viewProvider));
        addSubcommand(new FlagToggleCommand(viewProvider, keyParser));
        addSubcommand(new ZombiesRejoinCommand(viewProvider, joiner));
    }
}
