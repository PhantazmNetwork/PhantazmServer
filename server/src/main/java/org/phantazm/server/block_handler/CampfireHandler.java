package org.phantazm.server.block_handler;

import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class CampfireHandler implements BlockHandler {
    public static final CampfireHandler INSTANCE = new CampfireHandler();
    public static final NamespaceID NAMESPACE_ID = NamespaceID.from("minecraft:campfire");
    private static final Collection<Tag<?>> TAGS = List.of(Tag.ItemStack("Items").list());

    private CampfireHandler() {
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
