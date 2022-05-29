package com.github.phantazmnetwork.commons.databind;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public interface Property<TData> {
    interface Listener<TData> {
        void onPropertyChange(@Nullable TData oldValue, TData newValue);
    }

    void set(TData data);

    TData get();

    void addListener(@NotNull Listener<TData> onPropertyChange);

    static <TData> @NotNull Property<TData> of(TData initial) {
        return new Property<>() {
            private final List<Listener<TData>> listeners = new ArrayList<>();
            private TData current = initial;

            @Override
            public void set(TData data) {
                if (data != current) {
                    try {
                        for(Listener<TData> listener : listeners) {
                            listener.onPropertyChange(current, data);
                        }
                    }
                    finally {
                        //update value even if a listener throws
                        current = data;
                    }
                }
            }

            @Override
            public TData get() {
                return current;
            }

            @Override
            public void addListener(@NotNull Listener<TData> listener) {
                listeners.add(Objects.requireNonNull(listener, "listener"));
                listener.onPropertyChange(null, current);
            }
        };
    }

    static <TData> @NotNull Property<TData> of() {
        return of(null);
    }
}
