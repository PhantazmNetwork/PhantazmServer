package org.phantazm.server;

import net.minestom.server.instance.block.BlockManager;
import org.phantazm.server.block_handler.SignHandler;
import org.phantazm.server.block_handler.SkullHandler;

public class BlockHandlerFeature {

    static void initialize(BlockManager blockManager) {
        blockManager.registerHandler(SignHandler.NAMESPACE_ID, SignHandler::new);
        blockManager.registerHandler(SkullHandler.NAMESPACE_ID, SkullHandler::new);
    }

}
