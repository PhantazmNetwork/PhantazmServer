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
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GradientActionBarChooser implements ReloadActionBarChooser {

    public record Data(@NotNull Component component, @NotNull RGBLike from, @NotNull RGBLike to) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.action_bar.chooser.gradient");

        public Data {
            Objects.requireNonNull(component, "component");
            Objects.requireNonNull(from, "from");
            Objects.requireNonNull(to, "to");
        }

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

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
                node.put("message", componentProcessor.elementFromData(data.component()));
                node.put("from", rgbLikeProcessor.elementFromData(data.from()));
                node.put("to", rgbLikeProcessor.elementFromData(data.to()));

                return node;
            }
        };
    }

    private final Data data;

    public GradientActionBarChooser(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @NotNull Component choose(@NotNull GunState state, @NotNull Player player, float progress) {
        return data.component().color(TextColor.lerp(progress, data.from(), data.to()));
    }

    @Override
    public @NotNull Keyed getData() {
        return data;
    }
}
