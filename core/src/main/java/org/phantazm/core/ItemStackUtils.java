package org.phantazm.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.parser.SNBTParser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public final class ItemStackUtils {
    public static @NotNull ItemStack buildItem(@NotNull Material material, @Nullable String tag,
            @Nullable String displayName, @Nullable List<String> lore, @NotNull TagResolver @NotNull ... tags) {
        ItemStack.Builder builder = ItemStack.builder(material);
        if (tag != null) {
            try {
                builder.meta((NBTCompound)new SNBTParser(new StringReader(tag)).parse());
            }
            catch (NBTException ignored) {
            }
        }

        if (displayName != null) {
            builder.displayName(MiniMessage.miniMessage().deserialize(displayName, tags));
        }

        if (lore != null) {
            List<Component> components = new ArrayList<>(lore.size());
            for (String format : lore) {
                components.add(MiniMessage.miniMessage().deserialize(format, tags));
            }
            builder.lore(components);
        }

        return builder.build();
    }
}
