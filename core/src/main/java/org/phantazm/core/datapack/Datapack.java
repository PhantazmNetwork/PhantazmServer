package org.phantazm.core.datapack;

import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record Datapack(@NotNull Map<NamespaceID, Biome> biomes) {

}
