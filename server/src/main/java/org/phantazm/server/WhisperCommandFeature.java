package org.phantazm.server;

import com.github.steanky.ethylene.codec.toml.TomlCodec;
import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.bridge.Configuration;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import com.github.steanky.ethylene.mapper.type.Token;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.command.CommandManager;
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;
import org.phantazm.server.command.whisper.ReplyCommand;
import org.phantazm.server.command.whisper.WhisperCommand;
import org.phantazm.server.command.whisper.WhisperConfig;
import org.phantazm.server.command.whisper.WhisperManager;

import java.io.IOException;
import java.nio.file.Path;

public class WhisperCommandFeature {
    static void initialize(@NotNull CommandManager commandManager, @NotNull ConnectionManager connectionManager,
            @NotNull MappingProcessorSource mappingProcessorSource, @NotNull ConfigCodec whisperCodec)
            throws IOException {
        ConfigProcessor<WhisperConfig> whisperConfigProcessor =
                mappingProcessorSource.processorFor(Token.ofClass(WhisperConfig.class));
        String whisperFileName = whisperCodec.getPreferredExtensions().isEmpty()
                                 ? "whisper"
                                 : "whisper." + whisperCodec.getPreferredExtension();
        WhisperConfig whisperConfig =
                Configuration.read(Path.of(whisperFileName), whisperCodec,
                        whisperConfigProcessor);
        WhisperManager whisperManager =
                new WhisperManager(connectionManager, commandManager.getConsoleSender(), whisperConfig,
                        MiniMessage.miniMessage());

        commandManager.register(WhisperCommand.whisperCommand(connectionManager, whisperManager));
        commandManager.register(ReplyCommand.replyCommand(whisperManager));
    }

}
