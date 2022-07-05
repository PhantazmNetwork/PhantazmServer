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
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class StaticActionBarChooser implements ReloadActionBarChooser {

    public record Data(@NotNull Component message) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.action_bar.chooser.static");

        public Data {
            Objects.requireNonNull(message, "message");
        }

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<Component> componentProcessor = AdventureConfigProcessors.component();
        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Component message = componentProcessor.dataFromElement(element.getElementOrThrow("message"));
                return new Data(message);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(1);
                node.put("message", componentProcessor.elementFromData(data.message()));
                return node;
            }
        };
    }

    private final Data data;

    public StaticActionBarChooser(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @NotNull Component choose(@NotNull GunState state, @NotNull Player player, float progress) {
        return data.message();
    }

    @Override
    public @NotNull Keyed getData() {
        return data;
    }
}
