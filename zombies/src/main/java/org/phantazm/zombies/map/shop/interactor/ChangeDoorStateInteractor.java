package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.vector.Vec3I;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.VecUtils;
import org.phantazm.zombies.map.Door;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.map.shop.PlayerInteraction;
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
    public ChangeDoorStateInteractor(@NotNull Data data, @NotNull Supplier<? extends MapObjects> mapObjects) {
        super(data);
        this.mapObjects = Objects.requireNonNull(mapObjects, "mapObjects");
    }

    @Override
    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        if (!searchedDoor) {
            searchedDoor = true;

            Optional<Door> doorOptional = mapObjects.get().doorTracker().atPoint(VecUtils.toPoint(data.doorPosition));
            boolean isPresent = doorOptional.isPresent();
            if (isPresent) {
                door = doorOptional.get();
            }
            else {
                LOGGER.warn("Failed to locate door at {}", data.doorPosition);
                return;
            }
        }

        if (door != null) {
            switch (data.type) {
                case OPEN -> door.open();
                case CLOSE -> door.close();
                case TOGGLE -> {
                    if (door.isOpen()) {
                        door.close();
                    }
                    else {
                        door.open();
                    }
                }
            }
        }
        else {
            LOGGER.warn("Tried to open nonexistent door at {}", data.doorPosition);
        }
    }

    @DataObject
    public record Data(@NotNull Vec3I doorPosition, @NotNull OpenType type) {
    }

    public enum OpenType {
        OPEN,
        CLOSE,
        TOGGLE
    }
}
