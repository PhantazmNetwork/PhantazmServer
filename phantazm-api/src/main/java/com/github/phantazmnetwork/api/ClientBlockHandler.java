package com.github.phantazmnetwork.api;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * <p>Allows for "client blocks". These are blocks that exist purely on the client, they do not exist server-side. If a
 * client block is destroyed by the client, it is replaced by what is "actually" there according to the server.
 * Otherwise, the block will persist until it is removed. The server should be free to change its own block using
 * methods like {@link Instance#setBlock(int, int, int, Block)} without modifying what the client sees. Client blocks
 * must additionally persist through chunk unloads and reloads.</p>
 *
 * <p>When a client block is removed through {@link ClientBlockHandler#removeClientBlock(int, int, int)}, the actual
 * block is revealed to the client.</p>
 *
 * <p>Generally, it is expected that there should only exist one instance of this class per {@link Instance} that needs
 * it.</p>
 */
public interface ClientBlockHandler {
    /**
     * Sets or updates a client-only block at the given location.
     *
     * @param type the kind of block to set
     * @param x    the x-coordinate
     * @param y    the y-coordinate
     * @param z    the z-coordinate
     */
    void setClientBlock(@NotNull Block type, int x, int y, int z);

    /**
     * Convenience overload for {@link ClientBlockHandler#setClientBlock(Block, int, int, int)}.
     *
     * @param type          the kind of block to set
     * @param blockLocation the block location
     */
    default void setClientBlock(@NotNull Block type, @NotNull Vec3I blockLocation) {
        setClientBlock(type, blockLocation.getX(), blockLocation.getY(), blockLocation.getZ());
    }

    /**
     * Removes a client-only block, if it exists at the given location.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param z the z-coordinate
     */
    void removeClientBlock(int x, int y, int z);

    /**
     * Convenience overload for {@link ClientBlockHandler#removeClientBlock(int, int, int)}.
     *
     * @param blockLocation the block location
     */
    default void removeClientBlock(@NotNull Vec3I blockLocation) {
        removeClientBlock(blockLocation.getX(), blockLocation.getY(), blockLocation.getZ());
    }
}
