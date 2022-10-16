package com.github.phantazmnetwork.zombies.equipment.gun.target.headshot;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link HeadshotTester} that always headshots or always does not headshot.
 */
@Model("zombies.gun.headshot_tester.static")
public class StaticHeadshotTester implements HeadshotTester {

    private final Data data;

    /**
     * Creates a {@link StaticHeadshotTester}.
     *
     * @param data The {@link StaticHeadshotTester}'s {@link Data}
     */
    @FactoryMethod
    public StaticHeadshotTester(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public boolean isHeadshot(@NotNull Entity shooter, @NotNull Entity entity, @NotNull Point intersection) {
        return data.shouldHeadshot();
    }

    /**
     * Data for a {@link StaticHeadshotTester}.
     *
     * @param shouldHeadshot Whether the {@link StaticHeadshotTester} should always headshot
     */
    @DataObject
    public record Data(boolean shouldHeadshot) {

    }

}
