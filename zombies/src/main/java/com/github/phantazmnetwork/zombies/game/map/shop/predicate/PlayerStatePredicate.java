package com.github.phantazmnetwork.zombies.game.map.shop.predicate;

import com.github.phantazmnetwork.commons.ConfigProcessors;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.annotation.ProcessorMethod;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Model("zombies.map.shop.predicate.player_state")
public class PlayerStatePredicate extends PredicateBase<PlayerStatePredicate.Data> {
    @ProcessorMethod
    public static ConfigProcessor<PlayerStatePredicate.Data> processor() {
        return new ConfigProcessor<>() {
            private static final ConfigProcessor<Set<Key>> KEY_SET_PROCESSOR = ConfigProcessors.key().setProcessor();

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Set<Key> states = KEY_SET_PROCESSOR.dataFromElement(element.getElementOrThrow("states"));
                boolean blacklist = element.getBooleanOrThrow("blacklist");
                return new Data(states, blacklist);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(2);
                node.put("states", KEY_SET_PROCESSOR.elementFromData(data.states));
                node.putBoolean("blacklist", data.blacklist);
                return node;
            }
        };
    }

    @FactoryMethod
    public PlayerStatePredicate(@NotNull Data data) {
        super(data);
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        return data.blacklist != data.states.contains(interaction.getPlayer().getStateSwitcher().getState().key());
    }

    @DataObject
    public record Data(@NotNull Set<Key> states, boolean blacklist) {
    }
}
