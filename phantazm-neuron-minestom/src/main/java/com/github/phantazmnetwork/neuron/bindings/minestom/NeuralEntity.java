package com.github.phantazmnetwork.neuron.bindings.minestom;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.agent.Descriptor;
import com.github.phantazmnetwork.neuron.agent.Explorer;
import com.github.phantazmnetwork.neuron.engine.PathEngine;
import com.github.phantazmnetwork.neuron.navigator.BasicNavigator;
import com.github.phantazmnetwork.neuron.navigator.Controller;
import com.github.phantazmnetwork.neuron.navigator.Navigator;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class NeuralEntity extends LivingEntity implements Agent {
    private final Navigator navigator;
    private final Explorer explorer;
    private final Descriptor descriptor;
    private final Controller controller;

    public NeuralEntity(@NotNull EntityType entityType, @NotNull UUID uuid, @NotNull PathEngine pathEngine,
                        @NotNull Explorer explorer, @NotNull Descriptor descriptor) {
        super(entityType, uuid);
        this.navigator = new BasicNavigator(pathEngine, this, 40, 100,
                20);
        this.explorer = explorer;
        this.descriptor = descriptor;
        this.controller = new MinestomController(this, 69);
    }

    @Override
    public void update(long time) {
        super.update(time);
        navigator.tick(time);
    }

    @Override
    public boolean hasStartPosition() {
        return getInstance() != null && !isDead();
    }

    @Override
    public @NotNull Vec3I getStartPosition() {
        //naive startPosition calculation
        Pos pos = getPosition();
        return Vec3I.of(pos.blockX(), pos.blockY(), pos.blockZ());
    }

    @Override
    public @NotNull Descriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public @NotNull Explorer getExplorer() {
        return explorer;
    }

    @Override
    public @NotNull Controller getController() {
        return controller;
    }

    public void setDestination(@Nullable Entity target) {
        if(target == null) {
            navigator.setDestination(null);
        }
        else {
            navigator.setDestination(() -> {
                Pos pos = target.getPosition();
                return Vec3I.of(pos.blockX(), pos.blockY(), pos.blockZ());
            });
        }
    }
}
