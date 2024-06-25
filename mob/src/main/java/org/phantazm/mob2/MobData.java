package org.phantazm.mob2;

import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

@Default("""
    {
      showNameTag=false,
      tags=[],
      equipment={},
      attributes={},
      attributeModifiers={},
      meta=null,
      skills=[],
      goals=[],
      extra={}
    }
    """)
public record MobData(@NotNull Key key,
    @NotNull EntityType type,
    @NotNull ConfigNode pathfinding,
    boolean showNameTag,
    @NotNull Set<Key> tags,
    @NotNull ConfigNode equipment,
    @NotNull ConfigNode attributes,
    @NotNull ConfigNode attributeModifiers,
    @Nullable MobMeta meta,
    @NotNull List<ConfigNode> skills,
    @NotNull List<ConfigNode> goals,
    @NotNull ConfigNode extra) implements Keyed {

}
