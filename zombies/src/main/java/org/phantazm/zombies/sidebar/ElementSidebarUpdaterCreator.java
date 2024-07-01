package org.phantazm.zombies.sidebar;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.dependency.DependencyModule;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.dependency.ModuleDependencyProvider;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.core.path.ConfigPath;
import net.kyori.adventure.text.Component;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.ElementUtils;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class ElementSidebarUpdaterCreator implements Function<ZombiesPlayer, SidebarUpdater> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElementSidebarUpdaterCreator.class);

    private static final Consumer<? super ElementException> HANDLER = ElementUtils.logging(LOGGER, "sidebar");

    private final DependencyModule sidebarModule;

    private final ElementContext sidebarContext;

    private final KeyParser keyParser;

    private final ConfigPath updaterPath;

    public ElementSidebarUpdaterCreator(@NotNull SidebarModule sidebarModule, @NotNull ElementContext sidebarContext,
        @NotNull KeyParser keyParser, @NotNull ConfigPath updaterPath) {
        this.sidebarModule = Objects.requireNonNull(sidebarModule);
        this.sidebarContext = Objects.requireNonNull(sidebarContext);
        this.keyParser = Objects.requireNonNull(keyParser);
        this.updaterPath = Objects.requireNonNull(updaterPath);
    }

    @Override
    public SidebarUpdater apply(ZombiesPlayer zombiesPlayer) {
        DependencyProvider composite =
            DependencyProvider.composite(new ModuleDependencyProvider(keyParser, sidebarModule),
                new ModuleDependencyProvider(keyParser, zombiesPlayer.module()));
        SidebarUpdater updater = sidebarContext.provide(updaterPath, composite, HANDLER,
            () -> new SidebarUpdater(new Sidebar(Component.empty()), List.of()));
        updater.start();
        return updater;
    }
}
