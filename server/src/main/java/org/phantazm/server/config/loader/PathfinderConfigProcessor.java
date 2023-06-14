package org.phantazm.server.config.loader;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;
import org.phantazm.server.config.server.PathfinderConfig;

import java.util.concurrent.TimeUnit;

public class PathfinderConfigProcessor implements ConfigProcessor<PathfinderConfig> {
    private static final ConfigProcessor<TimeUnit> TIME_UNIT_PROCESSOR = ConfigProcessor.enumProcessor(TimeUnit.class);

    @Override
    public PathfinderConfig dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        int threads = element.getNumberOrThrow("threads").intValue();
        boolean asyncMode = element.getBooleanOrThrow("asyncMode");
        int corePoolSize = element.getNumberOrThrow("corePoolSize").intValue();
        int maximumPoolSize = element.getNumberOrThrow("maximumPoolSize").intValue();
        int minimumRunnable = element.getNumberOrThrow("minimumRunnable").intValue();
        long keepAliveTime = element.getNumberOrThrow("keepAliveTime").longValue();
        TimeUnit keepAliveTimeUnit =
                TIME_UNIT_PROCESSOR.dataFromElement(element.getElementOrThrow("keepAliveTimeUnit"));

        return new PathfinderConfig(threads, asyncMode, corePoolSize, maximumPoolSize, minimumRunnable, keepAliveTime,
                keepAliveTimeUnit);
    }

    @Override
    public @NotNull ConfigElement elementFromData(PathfinderConfig pathfinderConfig) throws ConfigProcessException {
        return ConfigNode.of("threads", pathfinderConfig.threads(), "asyncMode", pathfinderConfig.asyncMode(),
                "corePoolSize", pathfinderConfig.corePoolSize(), "maximumPoolSize", pathfinderConfig.maximumPoolSize(),
                "minimumRunnable", pathfinderConfig.minimumRunnable(), "keepAliveTime",
                pathfinderConfig.keepAliveTime(), "keepAliveTimeUnit",
                TIME_UNIT_PROCESSOR.elementFromData(pathfinderConfig.keepAliveTimeUnit()));
    }
}
