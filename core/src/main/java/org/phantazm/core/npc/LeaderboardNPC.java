package org.phantazm.core.npc;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.commons.MonoComponent;
import org.phantazm.core.leaderboard.Leaderboard;

import java.util.UUID;

@Model("npc.leaderboard")
@Cache
public class LeaderboardNPC implements MonoComponent<NPC> {
    private final MonoComponent<Leaderboard> leaderboard;

    @FactoryMethod
    public LeaderboardNPC(@NotNull @Child("leaderboard") MonoComponent<Leaderboard> leaderboard) {
        this.leaderboard = leaderboard;
    }

    @Override
    public @NotNull NPC apply(@NotNull InjectionStore injectionStore) {
        return new Impl(leaderboard.apply(injectionStore));
    }

    @DataObject
    public record Data(@NotNull @ChildPath("leaderboard") String leaderboard) {
    }

    private record Impl(Leaderboard leaderboard) implements NPC {
        @Override
        public void handleInteraction(@NotNull Player interactor) {

        }

        @Override
        public void spawn(@NotNull Instance instance) {
            leaderboard.show(instance);
        }

        @Override
        public void despawn() {
            leaderboard.hide();
        }

        @Override
        public @Nullable UUID uuid() {
            return null;
        }

        @Override
        public void tick(long time) {

        }
    }
}
