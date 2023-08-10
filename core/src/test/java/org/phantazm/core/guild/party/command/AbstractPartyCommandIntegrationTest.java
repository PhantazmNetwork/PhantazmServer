package org.phantazm.core.guild.party.command;

import net.kyori.adventure.text.Component;
import net.minestom.testing.EnvTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.phantazm.core.guild.GuildHolder;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.player.IdentitySource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@EnvTest
public class AbstractPartyCommandIntegrationTest {

    protected PartyCommandConfig commandConfig =
            new PartyCommandConfig(Component.empty(), Component.empty(), Component.empty(), Component.empty(), "", "",
                    Component.empty(), Component.empty(), Component.empty(), Component.empty(), "", "",
                    Component.empty(), Component.empty(), "", Component.empty(), "", "", "", "", "", Component.empty());

    protected GuildHolder<Party> partyHolder;

    protected IdentitySource identitySource;

    @BeforeEach
    public void setup() {
        partyHolder = new GuildHolder<>(new HashMap<>(), new ArrayList<>());
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
