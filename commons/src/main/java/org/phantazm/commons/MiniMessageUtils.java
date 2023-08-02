package org.phantazm.commons;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

public class MiniMessageUtils {

    private MiniMessageUtils() {
        throw new UnsupportedOperationException();
    }

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

    public static @NotNull TagResolver list(@NotNull String key,
            @NotNull Iterable<? extends ComponentLike> components) {
        return TagResolver.resolver(key, (argumentQueue, context) -> {
            String separatorString = argumentQueue.pop().value();
            Component separator = context.deserialize(separatorString);

            Component result = Component.join(JoinConfiguration.separator(separator), components);
            return Tag.inserting(result);
        });
    }

}
