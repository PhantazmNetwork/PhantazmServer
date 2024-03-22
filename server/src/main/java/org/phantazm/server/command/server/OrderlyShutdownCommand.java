package org.phantazm.server.command.server;

import com.github.steanky.toolkit.collection.Wrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import net.minestom.server.permission.Permission;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.command.PermissionLockedCommand;
import org.phantazm.core.event.scene.SceneJoinEvent;
import org.phantazm.core.event.scene.SceneShutdownEvent;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.scene2.JoinToggleableScene;
import org.phantazm.core.scene2.SceneManager;
import org.phantazm.server.config.server.ShutdownConfig;

public class OrderlyShutdownCommand extends PermissionLockedCommand {
    public static final Permission PERMISSION = new Permission("admin.orderly_shutdown");

    private static final Component FRESH_ROUTE_MESSAGE = Component.text("Routing to fresh instance...", NamedTextColor.RED);
    private static final Component NOT_JOINABLE_MESSAGE = Component.text("Server is not joinable!", NamedTextColor.RED);

    private boolean initialized;
    private long shutdownStart;

    public OrderlyShutdownCommand(@NotNull ShutdownConfig shutdownConfig) {
        super("orderly_shutdown", PERMISSION);

        addSyntax((sender, context) -> {
            if (initialized) {
                sender.sendMessage("Orderly shutdown has already been initialized");
                return;
            }

            EventNode<Event> globalNode = MinecraftServer.getGlobalEventHandler();
            globalNode.addListener(SceneShutdownEvent.class, this::onSceneShutdown);
            globalNode.addListener(AsyncPlayerPreLoginEvent.class, event -> {
                event.getPlayer().kick(NOT_JOINABLE_MESSAGE);
            });
            globalNode.addListener(SceneJoinEvent.class, event -> {
                if (!event.result().successful()) {
                    return;
                }

                for (PlayerView view : event.players()) {
                    view.getPlayer().ifPresent(player -> player.kick(FRESH_ROUTE_MESSAGE));
                }
            });

            initialized = true;
            shutdownStart = System.currentTimeMillis();

            SceneManager.Global.instance().forEachScene(scene -> {
                if (scene.isGame() && scene instanceof JoinToggleableScene joinToggleable) {
                    joinToggleable.getAcquirable().sync(self -> ((JoinToggleableScene) self).setJoinable(false));
                    return;
                }

                scene.getAcquirable().sync(self -> {
                    for (Player player : self.getPlayers()) {
                        player.kick(FRESH_ROUTE_MESSAGE);
                    }
                });
            });

            MinecraftServer.getSchedulerManager().scheduleTask(() -> {
                long elapsedMs = System.currentTimeMillis() - shutdownStart;
                if (elapsedMs > shutdownConfig.forceShutdownWarningTime()) {
                    Audiences.all().sendMessage(shutdownConfig.forceShutdownMessage());
                } else {
                    Audiences.all().sendMessage(shutdownConfig.shutdownMessage());
                }
            }, TaskSchedule.immediate(), TaskSchedule.millis(shutdownConfig.warningInterval()));

            MinecraftServer.getSchedulerManager().scheduleTask(() -> {
                long elapsedMs = System.currentTimeMillis() - shutdownStart;
                if (elapsedMs > shutdownConfig.forceShutdownTime()) {
                    exit(); //exit even if we've got games
                    return;
                }

                if (noGamesActive()) {
                    exit();
                }
            }, TaskSchedule.immediate(), TaskSchedule.tick(20));

            if (noGamesActive()) {
                exit();
            }
        });
    }

    private boolean noGamesActive() {
        Wrapper<Boolean> result = Wrapper.of(true);

        SceneManager.Global.instance().forEachScene(scene -> {
            scene.getAcquirable().sync(self -> {
                if (self.preventsServerShutdown()) {
                    result.set(false);
                }
            });
        });

        return result.get();
    }

    private void onSceneShutdown(@NotNull SceneShutdownEvent event) {
        if (noGamesActive()) {
            exit();
        }
    }

    private void exit() {
        Thread thread = new Thread(() -> {
            System.exit(0);
        });

        thread.setName("Shutdown-Trigger");
        thread.start();
    }
}
