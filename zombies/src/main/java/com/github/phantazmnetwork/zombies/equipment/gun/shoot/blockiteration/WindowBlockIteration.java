package com.github.phantazmnetwork.zombies.equipment.gun.shoot.blockiteration;

import com.github.phantazmnetwork.core.VecUtils;
import com.github.phantazmnetwork.zombies.game.map.objects.MapObjects;
import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

@Model("zombies.gun.block_iteration.window")
public class WindowBlockIteration implements BlockIteration {

    private final Supplier<? extends MapObjects> mapObjects;

    @FactoryMethod
    public WindowBlockIteration(@NotNull @Dependency("zombies.dependency.map_object.map_objects")
    Supplier<? extends MapObjects> mapObjects) {
        this.mapObjects = Objects.requireNonNull(mapObjects, "mapObjects");
    }

    @Override
    public @NotNull Context createContext() {
        return new Context() {
            @SuppressWarnings("UnstableApiUsage")
            @Override
            public boolean isValidEndpoint(@NotNull Point blockLocation, @NotNull Shape shape) {
                return true;
            }

            @SuppressWarnings("UnstableApiUsage")
            @Override
            public boolean isValidIntersection(@NotNull Vec intersection, @NotNull Shape shape) {
                return mapObjects.get().windowAt(VecUtils.toBlockInt(intersection)).isEmpty();
            }
        };
    }

}
