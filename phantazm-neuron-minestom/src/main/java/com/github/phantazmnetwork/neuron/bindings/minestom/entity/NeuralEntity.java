package com.github.phantazmnetwork.neuron.bindings.minestom.entity;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.agent.Descriptor;
import com.github.phantazmnetwork.neuron.agent.Explorer;
import com.github.phantazmnetwork.neuron.agent.TranslationExplorer;
import com.github.phantazmnetwork.neuron.engine.PathContext;
import com.github.phantazmnetwork.neuron.navigator.BasicNavigator;
import com.github.phantazmnetwork.neuron.navigator.Controller;
import com.github.phantazmnetwork.neuron.navigator.Navigator;
import com.github.phantazmnetwork.neuron.node.NodeTranslator;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Root of all entities that use Neuron for pathfinding.
 */
public class NeuralEntity extends LivingEntity implements Agent {
    private final MinestomDescriptor entityType;

    private final Navigator navigator;
    private final Controller controller;
    private final Explorer explorer;

    public NeuralEntity(@NotNull MinestomDescriptor entityType, @NotNull UUID uuid, @NotNull PathContext context,
                        @NotNull NodeTranslator translator) {
        super(entityType.getEntityType(), uuid);
        this.entityType = entityType;
        this.navigator = new BasicNavigator(context.getEngine(), this, 1000,
                10000, 1000);
        this.controller = new EntityController(this, entityType.getSpeed());
        this.explorer = new TranslationExplorer(context.getCache(), entityType.getID(), translator);
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
        return entityType;
    }

    @Override
    public @NotNull Explorer getExplorer() {
        return explorer;
    }

    @Override
    public @NotNull Controller getController() {
        return controller;
    }

    public @NotNull Navigator getNavigator() {
        return navigator;
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
