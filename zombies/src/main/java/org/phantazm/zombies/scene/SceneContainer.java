package org.phantazm.zombies.scene;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface SceneContainer<TScene> {

    @NotNull Collection<TScene> getScenes();

}
