package com.github.phantazmnetwork.neuron.node.config;

import com.github.phantazmnetwork.neuron.node.Calculator;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

/**
 * {@link ConfigProcessor} for {@link Calculator}s.
 */
public class CalculatorConfigProcessor implements ConfigProcessor<Calculator> {

    private enum CalculatorType {
        SQUARED_DISTANCE
    }

    private static final ConfigProcessor<CalculatorType> CALCULATOR_TYPE_PROCESSOR = ConfigProcessor.enumProcessor(CalculatorType.class);

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    public Calculator dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        return switch (CALCULATOR_TYPE_PROCESSOR.dataFromElement(element)) {
            case SQUARED_DISTANCE -> Calculator.SQUARED_DISTANCE;
        };
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull Calculator calculator) throws ConfigProcessException {
        if (calculator == Calculator.SQUARED_DISTANCE) {
            return CALCULATOR_TYPE_PROCESSOR.elementFromData(CalculatorType.SQUARED_DISTANCE);
        }

        throw new ConfigProcessException("Unknown calculator");
    }
}
