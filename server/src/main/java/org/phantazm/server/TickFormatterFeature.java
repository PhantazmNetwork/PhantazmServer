package org.phantazm.server;

import com.github.steanky.element.core.context.ContextManager;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.time.AnalogTickFormatter;
import org.phantazm.core.time.DurationTickFormatter;
import org.phantazm.core.time.PrecisionSecondTickFormatter;

public class TickFormatterFeature {
    private TickFormatterFeature() {
    }

    static void initialize(@NotNull ContextManager contextManager) {
        contextManager.registerElementClass(AnalogTickFormatter.class);
        contextManager.registerElementClass(DurationTickFormatter.class);
        contextManager.registerElementClass(PrecisionSecondTickFormatter.class);
    }
}
