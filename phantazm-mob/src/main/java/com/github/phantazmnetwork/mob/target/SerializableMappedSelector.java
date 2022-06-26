package com.github.phantazmnetwork.mob.target;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public abstract class SerializableMappedSelector<TFrom, TTo> extends MappedSelector<TFrom, TTo> implements VariantSerializable {


    public SerializableMappedSelector(@NotNull TargetSelector<TFrom> delegate) {
        super(delegate);
        if (!(delegate instanceof VariantSerializable)) {
            throw new IllegalArgumentException("delegate is not variant serializable");
        }
    }

    @Override
    public @NotNull Key getSerialKey() {
        return ((VariantSerializable) getDelegate()).getSerialKey();
    }
}
