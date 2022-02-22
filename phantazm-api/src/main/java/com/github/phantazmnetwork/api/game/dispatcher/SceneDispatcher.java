package com.github.phantazmnetwork.api.game.dispatcher;

import com.github.phantazmnetwork.api.util.Tickable;
import org.jetbrains.annotations.NotNull;

public interface SceneDispatcher<T> extends Tickable {

    @NotNull DispatchResult dispatch(@NotNull T dispatchRequest);

    void setJoinable(boolean joinable);

}
