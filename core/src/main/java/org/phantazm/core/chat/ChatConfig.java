package org.phantazm.core.chat;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.guild.party.PartyChatChannel;

import java.util.Map;

public record ChatConfig(@NotNull Map<String, String> chatFormats) {

    public static final ChatConfig DEFAULT = new ChatConfig(Map.of("all", "", PartyChatChannel.CHANNEL_NAME, ""));

}
