package org.phantazm.server.context;

import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import org.jetbrains.annotations.NotNull;

public record EthyleneContext(@NotNull KeyParser keyParser,
    @NotNull MappingProcessorSource mappingProcessorSource,
    @NotNull ConfigCodec yamlCodec,
    @NotNull ConfigCodec tomlCodec) {
}
