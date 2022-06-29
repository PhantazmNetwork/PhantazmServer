package com.github.phantazmnetwork.neuron.bindings.minestom.entity.config;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.GroundMinestomDescriptor;
import com.github.phantazmnetwork.neuron.node.Calculator;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.EntityType;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link ConfigProcessor} for {@link GroundMinestomDescriptor}s.
 */
public class GroundMinestomDescriptorConfigProcessor implements ConfigProcessor<GroundMinestomDescriptor> {

    private static final ConfigProcessor<Key> KEY_PROCESSOR = AdventureConfigProcessors.key();

    private final ConfigProcessor<Calculator> calculatorProcessor;

    /**
     * Creates a new {@link GroundMinestomDescriptorConfigProcessor}.
     * @param calculatorProcessor A {@link ConfigProcessor} for {@link Calculator}s
     */
    public GroundMinestomDescriptorConfigProcessor(@NotNull ConfigProcessor<Calculator> calculatorProcessor) {
        this.calculatorProcessor = Objects.requireNonNull(calculatorProcessor, "calculatorProcessor");
    }

    @Override
    public GroundMinestomDescriptor dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        String id = element.getStringOrThrow("id");
        Calculator calculator = calculatorProcessor.dataFromElement(element.getElementOrThrow("calculator"));
        EntityType entityType = EntityType.fromNamespaceId(NamespaceID.from(KEY_PROCESSOR.dataFromElement(element.getElementOrThrow("entityType"))));
        float jumpHeight = element.getNumberOrThrow("jumpHeight").floatValue();
        float fallTolerance = element.getNumberOrThrow("fallTolerance").floatValue();
        float stepHeight = element.getNumberOrThrow("stepHeight").floatValue();

        return new GroundMinestomDescriptor() {
            @Override
            public @NotNull Calculator getCalculator() {
                return calculator;
            }

            @Override
            public @NotNull EntityType getEntityType() {
                return entityType;
            }

            @Override
            public @NotNull String getID() {
                return id;
            }

            @Override
            public float getJumpHeight() {
                return jumpHeight;
            }

            @Override
            public float getFallTolerance() {
                return fallTolerance;
            }

            @Override
            public float getStepHeight() {
                return stepHeight;
            }
        };
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull GroundMinestomDescriptor groundMinestomDescriptor) throws ConfigProcessException {
        ConfigNode node = new LinkedConfigNode();
        node.putString("id", groundMinestomDescriptor.getID());
        node.put("calculator", calculatorProcessor.elementFromData(groundMinestomDescriptor.getCalculator()));
        node.put("entityType", KEY_PROCESSOR.elementFromData(groundMinestomDescriptor.getEntityType().namespace()));
        node.putNumber("jumpHeight", groundMinestomDescriptor.getJumpHeight());
        node.putNumber("fallTolerance", groundMinestomDescriptor.getFallTolerance());
        node.putNumber("stepHeight", groundMinestomDescriptor.getStepHeight());

        return node;
    }
}
