package org.phantazm.zombies.map.shop.display.creator;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.ElementFactory;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.dependency.DependencyModule;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.dependency.ModuleDependencyProvider;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.element.core.path.ElementPath;
import com.github.steanky.ethylene.mapper.type.Token;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.ElementUtils;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.map.shop.display.EmptyDisplay;
import org.phantazm.zombies.map.shop.display.ShopDisplay;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Model("zombies.map.shop.display.creator.linked")
@Cache(false)
public class LinkedPlayerDisplayCreator implements PlayerDisplayCreator {
    private static final Consumer<? super ElementException> HANDLER =
            ElementUtils.logging(LoggerFactory.getLogger(LinkedPlayerDisplayCreator.class), "player shop display");

    private final ElementPath path;

    private final Supplier<? extends MapObjects> mapObjects;
    private final ElementContext shopContext;
    private final KeyParser keyParser;

    @FactoryMethod
    public static ElementFactory<Data, LinkedPlayerDisplayCreator> factory() {
        return new ElementFactory<>() {
            private static final DependencyProvider.TypeKey<Supplier<? extends MapObjects>> MAP_OBJECTS_KEY =
                    DependencyProvider.key(new Token<>() {
                    });
            private static final DependencyProvider.TypeKey<KeyParser> KEY_PARSER_KEY =
                    DependencyProvider.key(new Token<>() {
                    });

            @Override
            public @NotNull LinkedPlayerDisplayCreator make(Data objectData, @NotNull ElementPath dataPath,
                    @NotNull ElementContext context, @NotNull DependencyProvider dependencyProvider) {
                Supplier<? extends MapObjects> mapObjectsSupplier = dependencyProvider.provide(MAP_OBJECTS_KEY);
                KeyParser keyParser = dependencyProvider.provide(KEY_PARSER_KEY);

                return new LinkedPlayerDisplayCreator(objectData, dataPath, mapObjectsSupplier, context, keyParser);
            }
        };
    }

    public LinkedPlayerDisplayCreator(@NotNull Data data, @NotNull ElementPath dataPath,
            @NotNull Supplier<? extends MapObjects> mapObjects, @NotNull ElementContext shopContext,
            @NotNull KeyParser keyParser) {
        this.path = dataPath.resolve(ElementPath.of(data.path));
        this.mapObjects = Objects.requireNonNull(mapObjects, "mapObjects");
        this.shopContext = Objects.requireNonNull(shopContext, "shopContext");
        this.keyParser = Objects.requireNonNull(keyParser, "keyParser");
    }

    @Override
    public @NotNull ShopDisplay forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        MapObjects objects = mapObjects.get();

        DependencyProvider composite = DependencyProvider.composite(objects.mapDependencyProvider(),
                new ModuleDependencyProvider(keyParser, new Module(zombiesPlayer)));

        return shopContext.provide(path, composite, HANDLER, () -> EmptyDisplay.INSTANCE);
    }

    @DataObject
    public record Data(@NotNull String path) {
    }

    @Depend
    @Memoize
    public static class Module implements DependencyModule {
        private final ZombiesPlayer zombiesPlayer;

        private Module(@NotNull ZombiesPlayer zombiesPlayer) {
            this.zombiesPlayer = Objects.requireNonNull(zombiesPlayer, "zombiesPlayer");
        }

        public @NotNull ZombiesPlayer zombiesPlayer() {
            return zombiesPlayer;
        }
    }
}
