package org.phantazm.core.inventory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.UnaryOperator;

/**
 * Basic implementation of an {@link InventoryProfile}.
 */
public class BasicInventoryProfile implements InventoryProfile {
    private final AtomicReferenceArray<InventoryObject> objects;

    /**
     * Creates a basic inventory profile.
     *
     * @param slotCount The number of slots held by the profile (indexed by 0)
     */
    public BasicInventoryProfile(int slotCount) {
        this.objects = new AtomicReferenceArray<>(slotCount);
    }

    @Override
    public @Nullable InventoryObject getInventoryObject(int slot) {
        return objects.getAcquire(slot);
    }

    @Override
    public @Nullable InventoryObject setInventoryObject(int slot, @Nullable InventoryObject object) {
        return objects.getAndSet(slot, object);
    }

    @Override
    public boolean compareAndSet(int slot, @Nullable InventoryObject witness, @Nullable InventoryObject insert) {
        if (witness == null) {
            return objects.compareAndSet(slot, null, insert);
        }

        return Objects.equals(objects.getAndUpdate(slot, existing -> {
            if (Objects.equals(existing, witness)) return insert;
            else return existing;
        }), witness);
    }

    @Override
    public @Nullable InventoryObject getAndUpdate(int slot, UnaryOperator<InventoryObject> operator) {
        return objects.getAndUpdate(slot, operator);
    }

    @Override
    public int getSlotCount() {
        return objects.length();
    }

    @Override
    public @NotNull Iterable<? extends InventoryObject> objects() {
        return new Iterable<>() {
            @Override
            public @NotNull Iterator<InventoryObject> iterator() {
                return new Iterator<>() {
                    private int i = 0;

                    @Override
                    public boolean hasNext() {
                        return i < objects.length();
                    }

                    @Override
                    public InventoryObject next() {
                        return objects.getAcquire(i++);
                    }
                };
            }
        };
    }
}
