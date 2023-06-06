package org.phantazm.core.particle.data;

import net.kyori.adventure.key.Keyed;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public interface PositionSource extends Keyed {
    void write(@NotNull BinaryWriter binaryWriter);
}
