package com.github.phantazmnetwork.zombies.mapeditor.client.render;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public interface EditorGroup<TData> {
    void setVisible(boolean enabled);

    @NotNull String getTranslationKey();

    void addNew(@NotNull Key key, @NotNull TData data);

    TData getObject(@NotNull Key key);

    void updateObject(@NotNull Key key);

    void addObject(@NotNull Key key, @NotNull TData data);

    void removeObject(@NotNull Key key);
}
