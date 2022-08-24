package com.github.phantazmnetwork.zombies.game.map.shop.interactor;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Prioritized;
import com.github.phantazmnetwork.commons.config.PrioritizedProcessor;
import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

@Model("zombies.map.shop.interactor.flag_setting")
public class FlagSettingInteractor extends InteractorBase<FlagSettingInteractor.Data> {
    @FactoryMethod
    public FlagSettingInteractor(@NotNull Data data, @NotNull @Dependency("zombies.dependency.map") ZombiesMap map) {
        super(data, map);
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return new PrioritizedProcessor<>() {
            private static final ConfigProcessor<Key> KEY_PROCESSOR = AdventureConfigProcessors.key();

            @Override
            public @NotNull Data finishData(@NotNull ConfigNode node, int priority) throws ConfigProcessException {
                Key flag = KEY_PROCESSOR.dataFromElement(node.getElementOrThrow("flag"));
                boolean remove = node.getBooleanOrThrow("remove");
                return new Data(priority, flag, remove);
            }

            @Override
            public @NotNull ConfigNode finishNode(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(1);
                node.put("flag", KEY_PROCESSOR.elementFromData(data.flag));
                node.putBoolean("remove", data.remove);
                return node;
            }
        };
    }

    @Override
    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        if (data.remove) {
            map.clearFlag(data.flag);
        }
        else {
            map.setFlag(data.flag);
        }
    }

    @DataObject
    public record Data(int priority, @NotNull Key flag, boolean remove) implements Prioritized {
    }
}
