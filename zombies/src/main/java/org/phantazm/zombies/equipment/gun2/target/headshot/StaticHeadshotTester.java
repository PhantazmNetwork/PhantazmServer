package org.phantazm.zombies.equipment.gun2.target.headshot;

import com.github.steanky.element.core.annotation.DataObject;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.PlayerComponent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;

public class StaticHeadshotTester implements PlayerComponent<HeadshotTester> {

    private final Data data;

    public StaticHeadshotTester(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull HeadshotTester forPlayer(@NotNull ZombiesPlayer player, @NotNull InjectionStore injectionStore) {
        return (shooter, entity, intersection) -> data.shouldHeadshot();
    }

    @DataObject
    public record Data(boolean shouldHeadshot) {

    }

}
