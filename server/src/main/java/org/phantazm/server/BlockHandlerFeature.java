package org.phantazm.server;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.BlockManager;
import org.phantazm.server.block_handler.*;

public final class BlockHandlerFeature {
    private BlockHandlerFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize() {
        BlockManager blockManager = MinecraftServer.getBlockManager();
        blockManager.registerHandler(SignHandler.NAMESPACE_ID, SignHandler::new);
        blockManager.registerHandler(SkullHandler.NAMESPACE_ID, SkullHandler::new);
        blockManager.registerHandler(BannerHandler.NAMESPACE_ID, BannerHandler::new);
        blockManager.registerHandler(EndGatewayHandler.NAMESPACE_ID, EndGatewayHandler::new);
        blockManager.registerHandler(CampfireHandler.NAMESPACE_ID, CampfireHandler::new);
    }

}
