package com.github.phantazmnetwork.zombies.equipment.gun.reload.actionbar;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.RGBLike;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link ReloadActionBarChooser} which colors a {@link Component} based on reload progress with a gradient between two colors.
 */
public class GradientActionBarChooser implements ReloadActionBarChooser {

    /**
     * Data for a {@link GradientActionBarChooser}.
     * @param message The message to send
     * @param from The starting color of the gradient
     * @param to The ending color of the gradient
     */
    public record Data(@NotNull Component message, @NotNull RGBLike from, @NotNull RGBLike to) implements Keyed {

        /**
         * The serial {@link Key} of this {@link Data}.
         */
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.action_bar.chooser.gradient");

        /**
         * Creates a {@link Data}.
         * @param message The message to send
         * @param from The starting color of the gradient
         * @param to The ending color of the gradient
         */
        public Data {
            Objects.requireNonNull(message, "message");
            Objects.requireNonNull(from, "from");
            Objects.requireNonNull(to, "to");
        }

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<Component> componentProcessor = AdventureConfigProcessors.component();
        ConfigProcessor<RGBLike> rgbLikeProcessor = AdventureConfigProcessors.rgbLike();

        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Component message = componentProcessor.dataFromElement(element.getElementOrThrow("message"));
                RGBLike from = rgbLikeProcessor.dataFromElement(element.getElementOrThrow("from"));
                RGBLike to = rgbLikeProcessor.dataFromElement(element.getElementOrThrow("to"));

                return new Data(message, from, to);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(3);
                node.put("message", componentProcessor.elementFromData(data.message()));
                node.put("from", rgbLikeProcessor.elementFromData(data.from()));
                node.put("to", rgbLikeProcessor.elementFromData(data.to()));

                return node;
            }
        };
    }

    private final Data data;

    /**
     * Creates a new {@link GradientActionBarChooser} with the given {@link Data}.
     * @param data The {@link Data} to use
     */
    public GradientActionBarChooser(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @NotNull Component choose(@NotNull GunState state, float progress) {
        return data.message().color(TextColor.lerp(progress, data.from(), data.to()));
    }

}
