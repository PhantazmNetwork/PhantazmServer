package org.phantazm.zombies.command;

import com.github.steanky.element.core.key.KeyParser;
import net.minestom.server.command.builder.Command;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.party.Party;
import org.phantazm.loader.Loader;
import org.phantazm.stats.zombies.ZombiesStatsDatabase;
import org.phantazm.zombies.modifier.ModifierCommandConfig;
import org.phantazm.zombies.modifier.ModifierHandler;
import org.phantazm.zombies.scene2.ZombiesJoiner;
import org.phantazm.zombies.scene2.ZombiesSceneCreator;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ZombiesCommand extends Command {
    public ZombiesCommand(@NotNull ZombiesJoiner joiner, @NotNull Map<? super UUID, ? extends Party> parties,
        @NotNull KeyParser keyParser, @NotNull Loader<ZombiesSceneCreator> zombiesSceneCreators,
        long joinRatelimit, @NotNull ZombiesStatsDatabase zombiesDatabase, @NotNull ModifierCommandConfig commandConfig,
        @NotNull Loader<ModifierHandler> modifierHandlerLoader) {
        super("zombies", "z");

        Objects.requireNonNull(joiner);
        Objects.requireNonNull(parties);
        Objects.requireNonNull(keyParser);
        Objects.requireNonNull(zombiesSceneCreators);

        addSubcommand(new ZombiesJoinCommand(joiner, parties, keyParser, zombiesSceneCreators, modifierHandlerLoader,
            joinRatelimit, zombiesDatabase));
        addSubcommand(new CoinsCommand());
        addSubcommand(new RoundCommand());
        addSubcommand(new KillAllCommand());
        addSubcommand(new GodmodeCommand());
        addSubcommand(new AmmoRefillCommand());
        addSubcommand(new FlagToggleCommand(keyParser));
        addSubcommand(new ZombiesRejoinCommand(joiner));
        addSubcommand(new ModifierCommand(keyParser, commandConfig, modifierHandlerLoader));
    }
}
