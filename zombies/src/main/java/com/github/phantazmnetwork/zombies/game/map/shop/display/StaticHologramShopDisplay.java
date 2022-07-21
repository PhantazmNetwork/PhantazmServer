package com.github.phantazmnetwork.zombies.game.map.shop.display;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.component.KeyedConfigProcessor;
import com.github.phantazmnetwork.commons.component.annotation.ComponentData;
import com.github.phantazmnetwork.commons.component.annotation.ComponentFactory;
import com.github.phantazmnetwork.commons.component.annotation.ComponentModel;
import com.github.phantazmnetwork.commons.component.annotation.ComponentProcessor;
import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.zombies.game.map.shop.Shop;
import com.github.phantazmnetwork.zombies.map.HologramInfo;
import com.github.phantazmnetwork.zombies.map.MapProcessors;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

@ComponentModel("phantazm:zombies.map.shop.display.static_hologram")
public class StaticHologramShopDisplay extends HologramShopDisplayBase {
    private final Data data;

    private static final KeyedConfigProcessor<Data> PROCESSOR = new KeyedConfigProcessor<>() {
        @Override
        public @NotNull Data dataFromNode(@NotNull ConfigNode node) throws ConfigProcessException {
            HologramInfo info = MapProcessors.hologramInfo().dataFromElement(node.getElementOrThrow("hologramInfo"));
            return new Data(info);
        }

        @Override
        public @NotNull ConfigNode nodeFromData(@NotNull Data data) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(1);
            node.put("hologramInfo", MapProcessors.hologramInfo().elementFromData(data.info));
            return node;
        }
    };

    @ComponentProcessor
    public static @NotNull KeyedConfigProcessor<Data> processor() {
        return PROCESSOR;
    }

    @ComponentFactory
    public StaticHologramShopDisplay(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        Vec3I location = shop.getData().triggerLocation();
        Vec3D offset = data.info.position();
        Vec3D center = Vec3D.of(location.getX() + 0.5 + offset.getX(), location.getY() + 0.5 + offset.getY(),
                                location.getZ() + 0.5 + offset.getZ()
        );

        hologram.setLocation(center);
        hologram.setInstance(shop.getInstance());

        hologram.clear();
        hologram.addAll(data.info.text());
    }

    @ComponentData
    public record Data(@NotNull HologramInfo info) implements Keyed {
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "zombies.map.shop.display.static_hologram");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }
}
