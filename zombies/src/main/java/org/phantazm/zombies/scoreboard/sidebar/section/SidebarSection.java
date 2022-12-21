package org.phantazm.zombies.scoreboard.sidebar.section;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public interface SidebarSection {

    void invalidateCache();

    int getSize();

    @NotNull List<Optional<Component>> update(long time);

}
