package org.phantazm.zombies.sidebar.lineupdater.creator;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.kill.PlayerKills;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.sidebar.lineupdater.SidebarLineUpdater;

import java.util.Objects;
import java.util.Optional;

@Model("zombies.sidebar.line_updater.creator.kills")
@Cache(false)
public class ZombieKillsUpdaterCreator implements PlayerUpdaterCreator {
    private final Data data;

    @FactoryMethod
    public ZombieKillsUpdaterCreator(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
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
            this.data = Objects.requireNonNull(data);
            this.playerKills = Objects.requireNonNull(playerKills);
        }

        @Override
        public void invalidateCache() {
            killCount = -1;
        }

        @Override
        public @NotNull Optional<Component> tick(long time) {
            if (killCount == -1 || killCount != playerKills.getKills()) {
                killCount = playerKills.getKills();

                TagResolver killsPlaceholder = Placeholder.component("kills", Component.text(killCount));
                return Optional.of(MiniMessage.miniMessage().deserialize(data.format, killsPlaceholder));
            }

            return Optional.empty();
        }
    }

    @DataObject
    public record Data(@NotNull String format) {
    }
}
