package org.phantazm.snbt;

/*
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;*/

public class Builder {
    public static void main(String[] args) {
        /*/
        if (args.length <= 1) {
            System.out.println("Incorrect number of arguments; needs at least 2");
            System.exit(1);
            return;
        }

        String material = args[0];
        String name = args[1];
        List<String> lore = Arrays.stream(args, 2, args.length).toList();

        ItemStack itemStack =
            ItemStack.builder(Objects.requireNonNullElse(Material.fromNamespaceId(material), Material.AIR))
                .displayName(MiniMessage.miniMessage().deserialize(name))
                .lore(lore.stream().map(string -> MiniMessage.miniMessage().deserialize(string)).toList())
                .build();

        System.out.println(itemStack.toItemNBT().toSNBT());*/
    }
}
