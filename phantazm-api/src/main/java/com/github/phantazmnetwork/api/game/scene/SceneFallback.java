package com.github.phantazmnetwork.api.game.scene;

import com.github.phantazmnetwork.api.player.PlayerView;
import org.jetbrains.annotations.NotNull;

public interface SceneFallback {

    void fallback(@NotNull PlayerView player);

}
