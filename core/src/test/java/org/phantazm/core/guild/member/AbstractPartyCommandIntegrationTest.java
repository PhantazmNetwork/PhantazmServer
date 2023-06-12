package org.phantazm.core.guild.member;

import net.minestom.server.command.builder.Command;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyCreator;
import org.phantazm.core.guild.party.command.PartyCommand;
import org.phantazm.core.player.BasicPlayerViewProvider;
import org.phantazm.core.player.IdentitySource;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class AbstractPartyCommandIntegrationTest {

    protected Map<? super UUID, Party> parties;

    protected IdentitySource identitySource;

    @BeforeEach
    public void setup() {
        parties = new HashMap<>();
        identitySource = new IdentitySource() {
            @Override
            public @NotNull CompletableFuture<Optional<String>> getName(@NotNull UUID uuid) {
                return CompletableFuture.completedFuture(Optional.empty());
            }

            @Override
            public @NotNull CompletableFuture<Optional<UUID>> getUUID(@NotNull String name) {
                return CompletableFuture.completedFuture(Optional.empty());
            }
        };
    }

}
