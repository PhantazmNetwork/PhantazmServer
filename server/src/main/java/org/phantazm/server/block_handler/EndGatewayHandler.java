package org.phantazm.server.block_handler;

import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class EndGatewayHandler implements BlockHandler {
    public static final EndGatewayHandler INSTANCE = new EndGatewayHandler();
    public static final NamespaceID NAMESPACE_ID = NamespaceID.from("minecraft:end_gateway");
    private static final Collection<Tag<?>> TAGS = List.of(Tag.Long("Age"));

    private EndGatewayHandler() {
    }

    @Override
    public @NotNull NamespaceID getNamespaceId() {
        return NAMESPACE_ID;
    }

    @Override
    public @NotNull Collection<Tag<?>> getBlockEntityTags() {
        return TAGS;
    }
}
