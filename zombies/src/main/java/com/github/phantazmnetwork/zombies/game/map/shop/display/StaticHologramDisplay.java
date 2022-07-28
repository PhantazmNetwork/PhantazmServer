package com.github.phantazmnetwork.zombies.game.map.shop.display;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.zombies.game.map.shop.Shop;
import com.github.phantazmnetwork.zombies.map.HologramInfo;
import com.github.phantazmnetwork.zombies.map.MapProcessors;
import com.github.steanky.element.core.annotation.ElementData;
import com.github.steanky.element.core.annotation.ElementModel;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.ProcessorMethod;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

@ElementModel("zombies.map.shop.display.static_hologram")
public class StaticHologramDisplay extends HologramDisplayBase {
    private static final ConfigProcessor<Data> PROCESSOR = new ConfigProcessor<>() {
        private static final ConfigProcessor<HologramInfo> HOLOGRAM_PROCESSOR = MapProcessors.hologramInfo();

        @Override
        public @NotNull Data dataFromElement(@NotNull ConfigElement node) throws ConfigProcessException {
            HologramInfo info = HOLOGRAM_PROCESSOR.dataFromElement(node.getElementOrThrow("hologramInfo"));
            return new Data(info);
        }

        @Override
        public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(1);
            node.put("hologramInfo", HOLOGRAM_PROCESSOR.elementFromData(data.info));
            return node;
        }
    };
    private final Data data;

    @FactoryMethod
    public StaticHologramDisplay(@NotNull Data data) {
        this.data = data;
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return PROCESSOR;
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        Vec3I location = shop.getData().triggerLocation();
        Vec3D offset = data.info.position();
        Vec3D center = Vec3D.of(location.getX() + 0.5 + offset.getX(), location.getY() + 0.5 + offset.getY(),
                location.getZ() + 0.5 + offset.getZ());

        hologram.setLocation(center);
        hologram.setInstance(shop.getInstance());

        hologram.clear();
        hologram.addAll(data.info.text());
    }

    @ElementData
    public record Data(@NotNull HologramInfo info) implements Keyed {
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "zombies.map.shop.display.static_hologram");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }
}
