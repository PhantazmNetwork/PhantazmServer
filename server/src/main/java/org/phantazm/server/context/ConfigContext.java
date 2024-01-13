package org.phantazm.server.context;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.chat.ChatConfig;
import org.phantazm.core.guild.party.PartyConfig;
import org.phantazm.server.command.whisper.WhisperConfig;
import org.phantazm.server.config.lobby.LobbiesConfig;
import org.phantazm.server.config.player.PlayerConfig;
import org.phantazm.server.config.server.JoinReportConfig;
import org.phantazm.server.config.server.PathfinderConfig;
import org.phantazm.server.config.server.ServerConfig;
import org.phantazm.server.config.server.ShutdownConfig;
import org.phantazm.server.config.zombies.ZombiesConfig;
import org.phantazm.zombies.modifier.ModifierCommandConfig;

public record ConfigContext(@NotNull PlayerConfig playerConfig,
    @NotNull ServerConfig serverConfig,
    @NotNull ShutdownConfig shutdownConfig,
    @NotNull PathfinderConfig pathfinderConfig,
    @NotNull LobbiesConfig lobbiesConfig,
    @NotNull PartyConfig partyConfig,
    @NotNull WhisperConfig whisperConfig,
    @NotNull ChatConfig chatConfig,
    @NotNull JoinReportConfig joinReportConfig,
    @NotNull ZombiesConfig zombiesConfig,
    @NotNull ModifierCommandConfig modifierCommandConfig) {
}
