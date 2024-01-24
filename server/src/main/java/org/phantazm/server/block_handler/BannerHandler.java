package org.phantazm.server.block_handler;

import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class BannerHandler implements BlockHandler {
    public static final BannerHandler INSTANCE = new BannerHandler();
    public static final NamespaceID NAMESPACE_ID = NamespaceID.from("minecraft:banner");
    private static final Collection<Tag<?>> TAGS = List.of(Tag.Pattern("Patterns").list());

    private BannerHandler() {
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
