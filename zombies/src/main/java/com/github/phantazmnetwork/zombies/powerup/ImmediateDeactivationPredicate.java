package com.github.phantazmnetwork.zombies.powerup;

import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;

import java.util.function.Supplier;

@Model("zombies.powerup.deactivation_predicate.immediate")
public class ImmediateDeactivationPredicate implements Supplier<DeactivationPredicate> {
    public static final DeactivationPredicate INSTANCE = new Predicate();

    @FactoryMethod
    public ImmediateDeactivationPredicate() {
    }

    @Override
    public DeactivationPredicate get() {
        return INSTANCE;
    }

    private static class Predicate implements DeactivationPredicate {

        @Override
        public void activate(long time) {

        }

        @Override
        public boolean shouldDeactivate(long time) {
            return true;
        }
    }
}