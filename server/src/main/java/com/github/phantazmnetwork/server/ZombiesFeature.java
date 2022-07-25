package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.zombies.game.map.action.room.SpawnMobsAction;
import com.github.phantazmnetwork.zombies.game.map.action.round.AnnounceRoundAction;
import com.github.phantazmnetwork.zombies.game.map.shop.display.StaticHologramDisplay;
import com.github.phantazmnetwork.zombies.game.map.shop.interactor.FlagSettingInteractor;
import com.github.phantazmnetwork.zombies.game.map.shop.interactor.MessagingInteractor;
import com.github.phantazmnetwork.zombies.game.map.shop.interactor.PlaySoundInteractor;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.FlagPredicate;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.StaticCostPredicate;
import com.github.steanky.element.core.ElementBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public final class ZombiesFeature {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesFeature.class);
    private static ElementBuilder mapObjectBuilder;

    static void initialize(@NotNull ElementBuilder builder) {
        ZombiesFeature.mapObjectBuilder = Objects.requireNonNull(builder, "builder");
        registerComponentClasses(builder);
    }

    private static void registerComponentClasses(ElementBuilder builder) {
        LOGGER.info("Registering component classes...");
        //actions
        builder.registerElementClass(AnnounceRoundAction.class);
        builder.registerElementClass(SpawnMobsAction.class);

        //ShopPredicate
        builder.registerElementClass(StaticCostPredicate.class);
        builder.registerElementClass(FlagPredicate.class);

        //ShopInteractor
        builder.registerElementClass(FlagSettingInteractor.class);
        builder.registerElementClass(MessagingInteractor.class);
        builder.registerElementClass(PlaySoundInteractor.class);

        //ShopDisplay
        builder.registerElementClass(StaticHologramDisplay.class);

        LOGGER.info("Registered component classes.");
    }

    public static @NotNull ElementBuilder mapObjectBuilder() {
        if (mapObjectBuilder == null) {
            throw new IllegalStateException("ZombiesFeature has not been initialized yet");
        }

        return mapObjectBuilder;
    }
}
