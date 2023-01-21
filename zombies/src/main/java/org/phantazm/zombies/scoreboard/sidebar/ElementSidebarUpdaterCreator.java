package org.phantazm.zombies.scoreboard.sidebar;

import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.dependency.DependencyModule;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.dependency.ModuleDependencyProvider;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.element.core.path.ElementPath;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;
import java.util.function.Function;

public class ElementSidebarUpdaterCreator implements Function<ZombiesPlayer, SidebarUpdater> {

    private final DependencyModule sidebarModule;

    private final ElementContext sidebarContext;

    private final KeyParser keyParser;

    private final ElementPath updaterPath;

    public ElementSidebarUpdaterCreator(@NotNull SidebarModule sidebarModule, @NotNull ElementContext sidebarContext,
            @NotNull KeyParser keyParser, @NotNull ElementPath updaterPath) {
        this.sidebarModule = Objects.requireNonNull(sidebarModule, "sidebarModule");
        this.sidebarContext = Objects.requireNonNull(sidebarContext, "sidebarContext");
        this.keyParser = Objects.requireNonNull(keyParser, "keyParser");
        this.updaterPath = Objects.requireNonNull(updaterPath, "updaterPath");
    }

    @Override
    public SidebarUpdater apply(ZombiesPlayer zombiesPlayer) {
        DependencyProvider composite =
                DependencyProvider.composite(new ModuleDependencyProvider(keyParser, sidebarModule),
                        new ModuleDependencyProvider(keyParser, zombiesPlayer.getModule()));

        return sidebarContext.provide(updaterPath, composite, false);
    }
}