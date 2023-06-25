package org.phantazm.commons;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

public class MiniMessageUtils {

    public static @NotNull TagResolver optional(@NotNull String key, boolean present) {
        return TagResolver.resolver(key, (argumentQueue, context) -> {
            String message = argumentQueue.pop().value();
            Component result;
            if (present) {
                result = context.deserialize(message);
            } else {
                result = Component.empty();
            }

            return Tag.selfClosingInserting(result);
        });
    }

}
