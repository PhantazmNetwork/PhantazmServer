package org.phantazm.zombies.modifier;

import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@Default("""
    {
      displayName=null,
      abbreviatedDisplayName='',
      exclusiveModifiers=[],
      webhookEmoji=':game_die:',
      requiredPermissions=[],
      modifier={}
    }
    """)
public record ModifierData(int ordinal,
    @NotNull Key key,
    @Nullable Component displayName,
    @NotNull Component abbreviatedDisplayName,
    @NotNull ItemStack displayItem,
    @NotNull Set<Key> exclusiveModifiers,
    @NotNull String webhookEmoji,
    @NotNull Set<Permission> requiredPermissions,
    @NotNull ConfigNode modifier) {

    public @NotNull Component displayName() {
        return displayName == null ? Component.text(key.asString()) : displayName;
    }
}