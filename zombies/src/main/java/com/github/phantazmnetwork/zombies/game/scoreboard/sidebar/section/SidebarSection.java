package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.section;

import com.github.phantazmnetwork.zombies.game.ZombiesScene;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public interface SidebarSection {

    void invalidateCache();

    int getSize(@NotNull ZombiesScene scene);

    @NotNull List<Optional<Component>> update(long time);

}
