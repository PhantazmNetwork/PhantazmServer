package com.github.phantazmnetwork.zombies.mapeditor.client.render;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class EditorGroupAbstract<TData> implements EditorGroup<TData> {
    private final class Entry {
        private final TData data;
        private final ObjectRenderer.RenderObject renderObject;

        private Entry(TData data, ObjectRenderer.RenderObject renderObject) {
            this.data = data;
            this.renderObject = renderObject;
        }
    }

    private final ObjectRenderer renderer;
    private final String translationKey;
    private final Map<Key, Entry> objects;

    public EditorGroupAbstract(@NotNull ObjectRenderer renderer, @NotNull String translationKey) {
        this.renderer = Objects.requireNonNull(renderer, "renderer");
        this.translationKey = Objects.requireNonNull(translationKey, "translationKey");
        this.objects = new HashMap<>();
    }

    @Override
    public void setVisible(boolean visible) {
        for(Entry entry : objects.values()) {
            entry.renderObject.shouldRender = false;
        }
    }

    @Override
    public @NotNull String getTranslationKey() {
        return translationKey;
    }

    @Override
    public void addNew(@NotNull Key key, @NotNull TData data) {
        ObjectRenderer.RenderObject object = makeRenderObject(key, data);
        objects.put(key, new Entry(data, object));
        renderer.putObject(object);
    }

    @Override
    public TData getObject(@NotNull Key key) {
        Entry entry = objects.get(key);
        if(entry == null) {
            return null;
        }

        return entry.data;
    }

    @Override
    public void updateObject(@NotNull Key key) {
        Entry entry = objects.get(key);
        if(entry == null) {
            throw new IllegalArgumentException("No entry for key " + key + " exists");
        }

        ObjectRenderer.RenderObject renderObject = makeRenderObject(key, entry.data);
        renderer.putObject(renderObject);
    }

    @Override
    public void addObject(@NotNull Key key, @NotNull TData data) {
        ObjectRenderer.RenderObject renderObject = makeRenderObject(key, data);
        objects.put(key, new Entry(data, renderObject));
        renderer.putObject(renderObject);
    }

    @Override
    public void removeObject(@NotNull Key key) {
        objects.remove(key);
        renderer.removeObject(key);
    }

    protected abstract @NotNull ObjectRenderer.RenderObject makeRenderObject(@NotNull Key key, @NotNull TData data);
}
