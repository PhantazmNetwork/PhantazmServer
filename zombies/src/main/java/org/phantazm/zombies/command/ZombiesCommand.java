package org.phantazm.zombies.command;

import com.github.steanky.element.core.key.KeyParser;
import net.kyori.adventure.key.Key;
import net.minestom.server.command.builder.Command;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.party.Party;
import org.phantazm.stats.zombies.ZombiesDatabase;
import org.phantazm.zombies.map.MapInfo;
import org.phantazm.zombies.modifier.ModifierCommandConfig;
import org.phantazm.zombies.scene2.ZombiesJoiner;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ZombiesCommand extends Command {
    public ZombiesCommand(@NotNull ZombiesJoiner joiner, @NotNull Map<? super UUID, ? extends Party> parties,
        @NotNull KeyParser keyParser, @NotNull Map<Key, MapInfo> maps,
        long joinRatelimit, @NotNull ZombiesDatabase zombiesDatabase, @NotNull ModifierCommandConfig commandConfig) {
        super("zombies", "z");

        Objects.requireNonNull(joiner);
        Objects.requireNonNull(parties);
        Objects.requireNonNull(keyParser);
        Objects.requireNonNull(maps);

        addSubcommand(new ZombiesJoinCommand(joiner, parties, keyParser, maps, joinRatelimit,
            zombiesDatabase));
        addSubcommand(new CoinsCommand());
        addSubcommand(new RoundCommand());
        addSubcommand(new KillAllCommand());
        addSubcommand(new GodmodeCommand());
        addSubcommand(new AmmoRefillCommand());
        addSubcommand(new FlagToggleCommand(keyParser));
        addSubcommand(new ZombiesRejoinCommand(joiner));
        addSubcommand(new ModifierCommand(keyParser, commandConfig));
    }
}
