package org.phantazm.zombies.scoreboard.sidebar.lineupdater.creator;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.kill.PlayerKills;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scoreboard.sidebar.lineupdater.SidebarLineUpdater;

import java.util.Objects;
import java.util.Optional;

@Model("zombies.sidebar.line_updater.creator.kills")
@Cache
public class ZombieKillsUpdaterCreator implements PlayerUpdaterCreator {
    private final Data data;

    @FactoryMethod
    public ZombieKillsUpdaterCreator(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @NotNull SidebarLineUpdater forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        return new Updater(data, zombiesPlayer.module().getKills());
    }

    private static class Updater implements SidebarLineUpdater {
        private final Data data;
        private final PlayerKills playerKills;

        private int killCount = -1;

        public Updater(@NotNull Data data, @NotNull PlayerKills playerKills) {
            this.data = Objects.requireNonNull(data, "data");
            this.playerKills = Objects.requireNonNull(playerKills, "playerKills");
        }

        @Override
        public void invalidateCache() {
            killCount = -1;
        }

        @Override
        public @NotNull Optional<Component> tick(long time) {
            if (killCount == -1 || killCount != playerKills.getKills()) {
                killCount = playerKills.getKills();
                return Optional.of(MiniMessage.miniMessage().deserialize(String.format(data.formatString, killCount)));
            }

            return Optional.empty();
        }
    }

    @DataObject
    public record Data(@NotNull String formatString) {
    }
}
