package com.github.phantazmnetwork.api.chat.command;

import com.github.phantazmnetwork.api.chat.ChatChannel;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Argument class for a {@link Function} that provides {@link ChatChannel}s based on a {@link Player}.
 */
public class ArgumentChannel extends Argument<Function<Player, ChatChannel>> {

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
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        if (channelFinders.isEmpty()) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false,
                    false);
            argumentNode.parser = "brigadier:string";
            argumentNode.properties = BinaryWriter.makeArray(packetWriter -> packetWriter.writeVarInt(0));
            nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{ argumentNode });
        }
        else {
            DeclareCommandsPacket.Node[] nodes = new DeclareCommandsPacket.Node[channelFinders.size()];

            int i = 0;
            for (Iterator<String> iterator = channelFinders.keySet().iterator(); iterator.hasNext(); i++) {
                String name = iterator.next();

                DeclareCommandsPacket.Node argumentNode = new DeclareCommandsPacket.Node();
                argumentNode.flags = DeclareCommandsPacket.getFlag(DeclareCommandsPacket.NodeType.LITERAL, executable,
                        false, false);
                argumentNode.name = name;

                nodes[i] = argumentNode;
            }
            nodeMaker.addNodes(nodes);
        }
    }

}
