package org.phantazm.mob2;

import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Default("""
    {
      showNameTag=false,
      equipment={},
      attributes={},
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
    @NotNull ConfigNode equipment,
    @NotNull ConfigNode attributes,
    @Nullable MobMeta meta,
    @NotNull List<ConfigNode> skills,
    @NotNull List<ConfigNode> goals,
    @NotNull ConfigNode extra) implements Keyed {

}
