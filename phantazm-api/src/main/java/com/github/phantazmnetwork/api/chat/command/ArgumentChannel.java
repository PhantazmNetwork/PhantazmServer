package com.github.phantazmnetwork.api.chat.command;

import com.github.phantazmnetwork.api.chat.ChatChannel;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Argument class for a {@link Function} that provides {@link ChatChannel}s based on a {@link Player}.
 */
public class ArgumentChannel extends Argument<Function<Player, ChatChannel>> {

    /**
     * The error code representing an unknown channel.
     */
    public static final int UNKNOWN_CHANNEL = 1;

    private final Map<String, Function<Player, ChatChannel>> channelFinders;

    /**
     * Argument for a {@link ChatChannel} based on its name.
     *
     * @param id The id of the argument, used to retrieve the parsed value
     * @param channelFinders A map of channel names to a {@link Function}
     *                       that will get a {@link ChatChannel} for a specific {@link Player}
     */
    public ArgumentChannel(@NotNull String id, @NotNull Map<String, Function<Player, ChatChannel>> channelFinders) {
        super(id);
        this.channelFinders = Objects.requireNonNull(channelFinders, "channelFinders");
    }

    @Override
    public @NotNull Function<Player, ChatChannel> parse(@NotNull String input) throws ArgumentSyntaxException {
        Function<Player, ChatChannel> channelFinder = channelFinders.get(input);

        if (channelFinder == null) {
            throw new ArgumentSyntaxException("No channel finder is associated", input, UNKNOWN_CHANNEL);
        }

        return channelFinder;
    }

    @Override
    public String parser() {
        return "brigadier:string";
    }

    @Override
    public byte[] nodeProperties() {
        return BinaryWriter.makeArray(packetWriter -> {
            packetWriter.writeVarInt(0);
        });
    }
}
