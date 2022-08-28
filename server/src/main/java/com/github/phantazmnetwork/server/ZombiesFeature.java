package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.zombies.game.map.shop.gui.InteractingClickHandler;
import com.github.phantazmnetwork.zombies.game.map.shop.gui.StaticItemUpdater;
import com.github.phantazmnetwork.zombies.game.map.shop.interactor.OpenGuiInteractor;
import com.github.phantazmnetwork.zombies.game.map.action.room.SpawnMobsAction;
import com.github.phantazmnetwork.zombies.game.map.action.round.AnnounceRoundAction;
import com.github.phantazmnetwork.zombies.game.map.action.round.RevivePlayersAction;
import com.github.phantazmnetwork.zombies.game.map.shop.display.StaticHologramDisplay;
import com.github.phantazmnetwork.zombies.game.map.shop.interactor.*;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.*;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.logic.AndPredicate;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.logic.NotPredicate;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.logic.OrPredicate;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.logic.XorPredicate;
import com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.lineupdater.*;
import com.github.steanky.element.core.context.ContextManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public final class ZombiesFeature {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesFeature.class);
    private static ContextManager contextManager;

    static void initialize(@NotNull ContextManager contextManager) {
        ZombiesFeature.contextManager = Objects.requireNonNull(contextManager, "contextManager");
        registerComponentClasses(contextManager);
    }

    private static void registerComponentClasses(ContextManager contextManager) {
        LOGGER.info("Registering component classes...");
        //actions
        contextManager.registerElementClass(AnnounceRoundAction.class);
        contextManager.registerElementClass(RevivePlayersAction.class);
        contextManager.registerElementClass(SpawnMobsAction.class);

        //ShopPredicate
        contextManager.registerElementClass(StaticCostPredicate.class);
        contextManager.registerElementClass(FlagPredicate.class);
        contextManager.registerElementClass(PlayerStatePredicate.class);
        contextManager.registerElementClass(UuidPredicate.class);
        contextManager.registerElementClass(TypePredicate.class);

        contextManager.registerElementClass(AndPredicate.class);
        contextManager.registerElementClass(OrPredicate.class);
        contextManager.registerElementClass(NotPredicate.class);
        contextManager.registerElementClass(XorPredicate.class);

        //ShopInteractor
        contextManager.registerElementClass(FlagSettingInteractor.class);
        contextManager.registerElementClass(MessagingInteractor.class);
        contextManager.registerElementClass(PlaySoundInteractor.class);
        contextManager.registerElementClass(DelayedInteractor.class);
        contextManager.registerElementClass(ConditionalInteractor.class);
        contextManager.registerElementClass(OpenGuiInteractor.class);
        contextManager.registerElementClass(CloseGuiInteractor.class);

        //ShopDisplay
        contextManager.registerElementClass(StaticHologramDisplay.class);

        //Sidebar LineUpdaters
        contextManager.registerElementClass(CoinsSidebarLineUpdater.class);
        contextManager.registerElementClass(ConstantSidebarLineUpdater.class);
        contextManager.registerElementClass(JoinedPlayersSidebarLineUpdater.class);
        contextManager.registerElementClass(PlayerStateSidebarLineUpdater.class);
        contextManager.registerElementClass(RemainingZombiesSidebarLineUpdater.class);
        contextManager.registerElementClass(RoundSidebarLineUpdater.class);
        contextManager.registerElementClass(TicksLineUpdater.class);
        contextManager.registerElementClass(ZombieKillsSidebarLineUpdater.class);

        //ClickHandlers
        contextManager.registerElementClass(InteractingClickHandler.class);

        //ItemUpdater
        contextManager.registerElementClass(StaticItemUpdater.class);

        LOGGER.info("Registered component classes.");
    }

    public static @NotNull ContextManager mapObjectBuilder() {
        if (contextManager == null) {
            throw new IllegalStateException("ZombiesFeature has not been initialized yet");
        }

        return contextManager;
    }
}
