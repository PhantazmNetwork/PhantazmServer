package com.github.phantazmnetwork.zombies.map.shop.interactor;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.zombies.map.Door;
import com.github.phantazmnetwork.zombies.map.objects.MapObjects;
import com.github.phantazmnetwork.zombies.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@Model("zombies.map.shop.interactor.door_state")
@Cache(false)
public class ChangeDoorStateInteractor extends InteractorBase<ChangeDoorStateInteractor.Data> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeDoorStateInteractor.class);

    private final Supplier<? extends MapObjects> mapObjects;

    private Door door;
    private boolean searchedDoor;

    @FactoryMethod
    public ChangeDoorStateInteractor(@NotNull Data data,
            @NotNull @Dependency("zombies.dependency.map_object.map_objects")
            Supplier<? extends MapObjects> mapObjects) {
        super(data);
        this.mapObjects = Objects.requireNonNull(mapObjects, "mapObjects");
    }

    @Override
    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        if (!searchedDoor) {
            searchedDoor = true;

            Optional<Door> doorOptional = mapObjects.get().doorAt(data.doorPosition);
            boolean isPresent = doorOptional.isPresent();
            if (isPresent) {
                door = doorOptional.get();
            }
            else {
                LOGGER.warn("Failed to locate door at " + data.doorPosition);
                return;
            }
        }

        if (door != null) {
            if (data.open) {
                door.open();
            }
            else {
                door.close();
            }
        }
    }

    @DataObject
    public record Data(@NotNull Vec3I doorPosition, boolean open) {
    }
}
