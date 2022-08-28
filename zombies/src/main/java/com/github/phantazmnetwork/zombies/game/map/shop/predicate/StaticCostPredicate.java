package com.github.phantazmnetwork.zombies.game.map.shop.predicate;

import com.github.phantazmnetwork.commons.ConfigProcessors;
import com.github.phantazmnetwork.zombies.game.coin.ModifierSource;
import com.github.phantazmnetwork.zombies.game.coin.PlayerCoins;
import com.github.phantazmnetwork.zombies.game.coin.Transaction;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("zombies.map.shop.predicate.static_cost")
public class StaticCostPredicate extends PredicateBase<StaticCostPredicate.Data> {
    private final ModifierSource modifierSource;

    @FactoryMethod
    public StaticCostPredicate(@NotNull Data data,
            @NotNull @Dependency("zombies.dependency.map_object.modifier_source") ModifierSource modifierSource) {
        super(data);
        this.modifierSource = Objects.requireNonNull(modifierSource, "modifierSource");
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            private static final ConfigProcessor<Key> KEY_PROCESSOR = ConfigProcessors.key();

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                int cost = element.getNumberOrThrow("cost").intValue();
                Key modifierType = KEY_PROCESSOR.dataFromElement(element.getElementOrThrow("modifierType"));
                return new Data(cost, modifierType);
            }

            @Override
            public @NotNull ConfigNode elementFromData(@NotNull Data data) throws ConfigProcessException {
                return ConfigNode.of("cost", data.cost, "modifierType",
                        KEY_PROCESSOR.elementFromData(data.modifierType));
            }
        };
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        PlayerCoins coins = interaction.getPlayer().getCoins();
        return coins.runTransaction(new Transaction(modifierSource.modifiers(data.modifierType), -data.cost))
                .isAffordable(coins);
    }

    @DataObject
    public record Data(int cost, @NotNull Key modifierType) {
    }
}
