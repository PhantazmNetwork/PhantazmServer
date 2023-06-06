package org.phantazm.core.particle.data;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.vector.Vec3I;
import net.kyori.adventure.key.Key;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

@Model("particle.variant_data.position_source.block")
@Cache
public class BlockPositionSource implements PositionSource {
    private static final Key KEY = Key.key("block");

    private final Data data;

    @FactoryMethod
    public BlockPositionSource(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public void write(@NotNull BinaryWriter binaryWriter) {
        binaryWriter.writeBlockPosition(data.position.x(), data.position.y(), data.position.z());
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    @DataObject
    public record Data(@NotNull Vec3I position) {
    }
}
