package com.github.phantazmnetwork.zombies.game.map.shop.interactor;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.Prioritized;
import com.github.phantazmnetwork.commons.component.KeyedConfigProcessor;
import com.github.phantazmnetwork.commons.component.annotation.ComponentData;
import com.github.phantazmnetwork.commons.component.annotation.ComponentFactory;
import com.github.phantazmnetwork.commons.component.annotation.ComponentModel;
import com.github.phantazmnetwork.commons.component.annotation.ComponentProcessor;
import com.github.phantazmnetwork.commons.config.PrioritizedProcessor;
import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.map.shop.Shop;
import com.github.phantazmnetwork.zombies.map.ShopInfo;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

@ComponentModel("phantazm:zombies.map.shop.interactor.flag_setting")
public class FlagSettingInteractor extends InteractorBase<FlagSettingInteractor.Data> {
    private static final PrioritizedProcessor<Data> PROCESSOR = new PrioritizedProcessor<>() {
        @Override
        public @NotNull Data finishData(@NotNull ConfigNode node, int priority) throws ConfigProcessException {
            Key flag = AdventureConfigProcessors.key().dataFromElement(node.getElementOrThrow("flag"));
            return new Data(priority, flag);
        }

        @Override
        public @NotNull ConfigNode finishNode(@NotNull Data data) throws ConfigProcessException {
            ConfigNode node = new LinkedConfigNode(1);
            node.put("flag", AdventureConfigProcessors.key().elementFromData(data.flag));
            return node;
        }
    };

    @ComponentProcessor
    public static @NotNull KeyedConfigProcessor<Data> processor() {
        return PROCESSOR;
    }

    @ComponentFactory
    public FlagSettingInteractor(@NotNull Data data, @NotNull ShopInfo shopInfo, ZombiesMap.@NotNull Context context) {
        super(data, shopInfo, context);
    }

    @Override
    public void handleInteraction(@NotNull Shop shop, @NotNull PlayerInteraction interaction) {
        context.map().setFlag(data.flag);
    }

    @ComponentData
    public record Data(int priority, @NotNull Key flag) implements Keyed, Prioritized {
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "zombies.map.shop.interactor.flag_setting");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }
}
