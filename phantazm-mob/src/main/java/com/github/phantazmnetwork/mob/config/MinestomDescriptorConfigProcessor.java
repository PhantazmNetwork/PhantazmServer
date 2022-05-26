package com.github.phantazmnetwork.mob.config;

import com.github.phantazmnetwork.neuron.bindings.minestom.entity.MinestomDescriptor;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntity;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntityFactory;
import com.github.steanky.ethylene.core.ConfigElement;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.NotNull;

public interface MinestomDescriptorConfigProcessor {

    @NotNull Pair<MinestomDescriptor, NeuralEntityFactory<? super MinestomDescriptor, ? extends NeuralEntity>> descriptorFromElement(@NotNull ConfigElement element);

    @NotNull ConfigElement elementFromDescriptor(@NotNull MinestomDescriptor descriptor);

}
