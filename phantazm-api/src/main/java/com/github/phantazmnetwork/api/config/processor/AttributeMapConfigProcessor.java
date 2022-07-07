package com.github.phantazmnetwork.api.config.processor;

import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import org.jetbrains.annotations.NotNull;

public class AttributeMapConfigProcessor {

    public AttributeMapConfigProcessor() {
        throw new UnsupportedOperationException();
    }

    private static final ConfigProcessor<Object2FloatMap<String>> PROCESSOR
            = ConfigProcessor.FLOAT.mapProcessor(Object2FloatOpenHashMap::new);

    public static @NotNull ConfigProcessor<Object2FloatMap<String>> processor() {
        return PROCESSOR;
    }


}
