package org.phantazm.server.block_handler;

import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

// TODO: Reflect 1.20 changes
public class SignHandler implements BlockHandler {
    public static final SignHandler INSTANCE = new SignHandler();
    public static final NamespaceID NAMESPACE_ID = NamespaceID.from("minecraft:sign");
    private static final Collection<Tag<?>> TAGS = List.of(Tag.Boolean("GlowingText"), Tag.String("Color"),
        Tag.String("Text1"), Tag.String("Text2"), Tag.String("Text3"), Tag.String("Text4"));

    private SignHandler() {
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
