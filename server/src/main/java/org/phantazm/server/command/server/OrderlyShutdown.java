package org.phantazm.server.command.server;

import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.command.builder.Command;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.permission.Permission;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.RouterStore;
import org.phantazm.core.game.scene.SceneRouter;
import org.phantazm.core.game.scene.event.SceneShutdownEvent;
import org.phantazm.server.PhantazmServer;
import org.phantazm.server.config.server.ServerConfig;

import java.util.Objects;

public class OrderlyShutdown extends Command {
    public static final Permission PERMISSION = new Permission("admin.orderly_shutdown");

    private final RouterStore routerStore;
    private boolean initialized;

    public OrderlyShutdown(@NotNull RouterStore routerStore, @NotNull ServerConfig serverConfig,
            @NotNull EventNode<Event> globalNode) {
        super("orderly_shutdown");
        this.routerStore = Objects.requireNonNull(routerStore, "routerStore");

        setCondition((sender, commandString) -> sender.hasPermission(PERMISSION));
        addConditionalSyntax(getCondition(), (sender, context) -> {
            if (initialized) {
                sender.sendMessage("Orderly shutdown has already been initialized");
                return;
            }

            initialized = true;

            MinecraftServer.getSchedulerManager().scheduleTask(() -> {
                Audiences.all().sendMessage(serverConfig.shutdownMessage());
            }, TaskSchedule.immediate(), TaskSchedule.seconds(30));

            for (SceneRouter<?, ?> router : routerStore.getRouters()) {
                if (router.isGame()) {
                    router.setJoinable(false);
                }
            }

            globalNode.addListener(SceneShutdownEvent.class, this::onSceneShutdown);
        });
    }

    private void onSceneShutdown(@NotNull SceneShutdownEvent event) {
        boolean anyGames = false;
        for (SceneRouter<?, ?> router : routerStore.getRouters()) {
            if (!router.isGame()) {
                continue;
            }

            if (router.hasScenes()) {
                anyGames = true;
                break;
            }
        }

        if (!anyGames) {
            PhantazmServer.shutdown("orderly shutdown completion");
        }
    }
}
