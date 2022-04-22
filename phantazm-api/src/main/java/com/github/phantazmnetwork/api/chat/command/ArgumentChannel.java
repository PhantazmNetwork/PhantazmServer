package com.github.phantazmnetwork.api.chat.command;

import com.github.phantazmnetwork.api.chat.ChatChannel;
import com.github.phantazmnetwork.api.chat.ChatChannelStore;
import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Argument class for a {@link ChatChannel}.
 */
public class ArgumentChannel extends Argument<Pair<String, ChatChannel>> {

    public static final int UNKNOWN_CHANNEL = 1;

    private final ChatChannelStore channelStore;

    /**
     * Argument for a {@link ChatChannel} based on its name.
     *
     * @param id The id of the argument, used to retrieve the parsed value
     * @param channelStore A {@link ChatChannelStore} used to find currently registered {@link ChatChannel}s
     */
    public ArgumentChannel(@NotNull String id, @NotNull ChatChannelStore channelStore) {
        super(id);
        this.channelStore = Objects.requireNonNull(channelStore, "channelStore");
    }

    @Override
    public @NotNull Pair<String, ChatChannel> parse(@NotNull String input) throws ArgumentSyntaxException {
        ChatChannel channel = channelStore.getChannels().get(input);

        if (channel == null) {
            throw new ArgumentSyntaxException("No channel is associated", input, UNKNOWN_CHANNEL);
        }

        return Pair.of(input, channel);
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        Map<String, ChatChannel> channels = channelStore.getChannels();

        if (channels.isEmpty()) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false,
                    false);
            argumentNode.parser = "brigadier:string";
            argumentNode.properties = BinaryWriter.makeArray(packetWriter -> packetWriter.writeVarInt(0));
            nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{ argumentNode });
        }
        else {
            DeclareCommandsPacket.Node[] nodes = new DeclareCommandsPacket.Node[channels.size()];

            int i = 0;
            for (Iterator<String> iterator = channels.keySet().iterator(); iterator.hasNext(); i++) {
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
