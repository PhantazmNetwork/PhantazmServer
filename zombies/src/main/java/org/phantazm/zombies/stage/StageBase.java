package org.phantazm.zombies.stage;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Activable;

import java.util.Collection;
import java.util.List;

public abstract class StageBase implements Stage {

    private final Collection<Activable> activables;

    public StageBase(@NotNull Collection<Activable> activables) {
        this.activables = List.copyOf(activables);
    }

    @Override
    public void start() {
        for (Activable activable : activables) {
            activable.start();
        }
    }

    @Override
    public void tick(long time) {
        for (Activable activable : activables) {
            activable.tick(time);
        }
    }

    @Override
    public void end() {
        for (Activable activable : activables) {
            activable.end();
        }
    }
}
