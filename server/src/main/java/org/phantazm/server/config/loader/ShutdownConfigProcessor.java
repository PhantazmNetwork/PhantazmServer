package org.phantazm.server.config.loader;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.phantazm.server.config.server.ShutdownConfig;

public class ShutdownConfigProcessor implements ConfigProcessor<ShutdownConfig> {
    @Override
    public ShutdownConfig dataFromElement(@NotNull ConfigElement element) {
        Component shutdownMessage = MiniMessage.miniMessage().deserialize(element.getStringOrDefault(
            () -> MiniMessage.miniMessage().serialize(ShutdownConfig.DEFAULT.shutdownMessage()),
            "shutdownMessage"));

        Component forceShutdownMessage = MiniMessage.miniMessage().deserialize(element.getStringOrDefault(
            () -> MiniMessage.miniMessage().serialize(ShutdownConfig.DEFAULT.forceShutdownMessage()),
            "forceShutdownMessage"));

        long warningInterval =
            element.getNumberOrDefault(ShutdownConfig.DEFAULT.warningInterval(), "warningInterval").longValue();

        long forceShutdownTime =
            element.getNumberOrDefault(ShutdownConfig.DEFAULT.forceShutdownTime(), "forceShutdownTime").longValue();

        long forceShutdownWarningTime = element.getNumberOrDefault(ShutdownConfig.DEFAULT.forceShutdownWarningTime(),
            "forceShutdownWarningTime").longValue();

        return new ShutdownConfig(shutdownMessage, forceShutdownMessage, warningInterval, forceShutdownTime,
            forceShutdownWarningTime);
    }

    @Override
    public @NotNull ConfigElement elementFromData(ShutdownConfig shutdownConfig) {
        ConfigNode configNode = ConfigNode.of();
        configNode.putString("shutdownMessage", MiniMessage.miniMessage().serialize(shutdownConfig.shutdownMessage()));
        configNode.putString("forceShutdownMessage",
            MiniMessage.miniMessage().serialize(shutdownConfig.forceShutdownMessage()));
        configNode.putNumber("warningInterval", shutdownConfig.warningInterval());
        configNode.putNumber("forceShutdownTime", shutdownConfig.forceShutdownTime());
        configNode.putNumber("forceShutdownWarningTime", shutdownConfig.forceShutdownWarningTime());

        return configNode;
    }
}
