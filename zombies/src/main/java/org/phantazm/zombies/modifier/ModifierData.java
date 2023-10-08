package org.phantazm.zombies.modifier;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public record ModifierData(int ordinal,
    @NotNull Key key,
    @Nullable Component displayName,
    @NotNull ItemStack displayItem,
    @NotNull Set<Key> exclusiveModifiers,
    @NotNull String webhookEmoji,
    @NotNull ConfigNode modifier) {
    @Default("displayName")
    public static @NotNull ConfigElement defaultDisplayName() {
        return ConfigPrimitive.NULL;
    }

    @Default("exclusiveModifiers")
    public static @NotNull ConfigElement defaultExclusiveModifiers() {
        return ConfigList.of();
    }

    @Default("webhookEmoji")
    public static @NotNull ConfigElement defaultWebhookEmoji() {
        return ConfigPrimitive.of("game_die");
    }

    @Default("modifier")
    public static @NotNull ConfigElement defaultModifier() {
        return ConfigNode.of();
    }

    public @NotNull Component displayName() {
        return displayName == null ? Component.text(key.asString()) : displayName;
    }
}