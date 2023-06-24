package org.phantazm.core.game.scene;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;

import java.util.*;
import java.util.function.Function;

public class SceneTransferHelper {

    private final Function<? super UUID, Optional<? extends Scene<?>>> sceneMapper;

    public SceneTransferHelper(@NotNull Function<? super UUID, Optional<? extends Scene<?>>> sceneMapper) {
        this.sceneMapper = Objects.requireNonNull(sceneMapper, "sceneMapper");
    }

    public <TJoinRequest extends SceneJoinRequest> void transfer(@NotNull Scene<TJoinRequest> to,
            @NotNull TJoinRequest joinRequest, @NotNull Collection<PlayerView> leavers, @NotNull PlayerView leader) {
        boolean anyFailures = false;
        Collection<Runnable> leaveExecutors = new ArrayList<>(leavers.size());
        for (PlayerView leaver : leavers) {
            Optional<? extends Scene<?>> oldSceneOptional = sceneMapper.apply(leaver.getUUID());
            if (oldSceneOptional.isEmpty()) {
                continue;
            }

            Scene<?> oldScene = oldSceneOptional.get();
            if (oldScene == to) {
                continue;
            }

            TransferResult leaveResult = oldScene.leave(Collections.singleton(leaver.getUUID()));
            if (leaveResult.executor().isPresent()) {
                leaveExecutors.add(leaveResult.executor().get());
            }
            else {
                anyFailures = true;
                leaveResult.message().ifPresent(message -> {
                    leaver.getPlayer().ifPresent(player -> player.sendMessage(message));
                });
            }
        }

        if (anyFailures) {
            leader.getPlayer().ifPresent(leaderPlayer -> {
                leaderPlayer.sendMessage(Component.text("Failed to join because not all players could leave their " +
                        "previous scenes.", NamedTextColor.RED));
            });
            return;
        }

        for (Runnable leaveExecutor : leaveExecutors) {
            leaveExecutor.run();
        }

        TransferResult joinResult = to.join(joinRequest);
        if (joinResult.executor().isPresent()) {
            joinResult.executor().get().run();
        } else if (joinResult.message().isPresent()) {
            leader.getPlayer().ifPresent(leaderPlayer -> {
                leaderPlayer.sendMessage(joinResult.message().get());
            });
        }
    }

}
