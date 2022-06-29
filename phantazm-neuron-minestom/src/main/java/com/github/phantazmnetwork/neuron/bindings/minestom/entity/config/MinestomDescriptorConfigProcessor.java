package com.github.phantazmnetwork.neuron.bindings.minestom.entity.config;

import com.github.phantazmnetwork.neuron.bindings.minestom.entity.GroundMinestomDescriptor;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.MinestomDescriptor;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class MinestomDescriptorConfigProcessor implements ConfigProcessor<MinestomDescriptor> {

    @Override
    public MinestomDescriptor dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        switch (element.getStringOrThrow("type").toUpperCase()) {
            case "GROUND" -> {
                EntityType type = EntityType.fromNamespaceId(element.getStringOrThrow("entityType"));
                String ID = element.getStringOrThrow("ID");
                return GroundMinestomDescriptor.of(type, ID);
            }
            default -> throw new ConfigProcessException("unknown type");
        }
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull MinestomDescriptor descriptor) throws ConfigProcessException {
        if (descriptor instanceof GroundMinestomDescriptor) {
            ConfigNode node = new LinkedConfigNode(2);
            node.put("entityType", new ConfigPrimitive(descriptor.getEntityType().namespace().asString()));
            node.put("ID", new ConfigPrimitive(descriptor.getID()));
            return node;
        }

        throw new ConfigProcessException("unknown descriptor type");
    }
}
