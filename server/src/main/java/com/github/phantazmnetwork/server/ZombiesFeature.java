package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.commons.component.ComponentBuilder;
import com.github.phantazmnetwork.commons.component.ComponentException;
import com.github.phantazmnetwork.zombies.game.map.action.room.SpawnMobsAction;
import com.github.phantazmnetwork.zombies.game.map.action.round.AnnounceRoundAction;
import com.github.phantazmnetwork.zombies.game.map.shop.display.StaticHologramShopDisplay;
import com.github.phantazmnetwork.zombies.game.map.shop.interactor.FlagSettingInteractor;
import com.github.phantazmnetwork.zombies.game.map.shop.interactor.MessagingInteractor;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.FlagPredicate;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.StaticCostPredicate;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public final class ZombiesFeature {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesFeature.class);
    private static ComponentBuilder builder;

    static void initialize(@NotNull ComponentBuilder builder) throws ComponentException {
        ZombiesFeature.builder = Objects.requireNonNull(builder, "builder");
        registerComponentClasses(builder);
    }

    private static void registerComponentClasses(ComponentBuilder builder) throws ComponentException {
        LOGGER.info("Registering component classes...");
        //actions
        builder.registerComponentClass(AnnounceRoundAction.class);
        builder.registerComponentClass(SpawnMobsAction.class);

        //shops

        //predicate
        builder.registerComponentClass(StaticCostPredicate.class);
        builder.registerComponentClass(FlagPredicate.class);

        //interactor
        builder.registerComponentClass(FlagSettingInteractor.class);
        builder.registerComponentClass(MessagingInteractor.class);

        //display
        builder.registerComponentClass(StaticHologramShopDisplay.class);

        LOGGER.info("Registered component classes.");
    }

    public static @NotNull ComponentBuilder getBuilder() {
        if (builder == null) {
            throw new IllegalStateException("ZombiesFeature has not been initialized yet");
        }

        return builder;
    }
}
