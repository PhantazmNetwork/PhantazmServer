package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.core.item.AnimatedUpdatingItem;
import com.github.phantazmnetwork.core.item.StaticUpdatingItem;
import com.github.phantazmnetwork.zombies.game.map.action.room.SpawnMobsAction;
import com.github.phantazmnetwork.zombies.game.map.action.round.AnnounceRoundAction;
import com.github.phantazmnetwork.zombies.game.map.action.round.RevivePlayersAction;
import com.github.phantazmnetwork.zombies.game.map.shop.LinearUpgradePath;
import com.github.phantazmnetwork.zombies.game.map.shop.display.*;
import com.github.phantazmnetwork.zombies.game.map.shop.gui.InteractingClickHandler;
import com.github.phantazmnetwork.zombies.game.map.shop.interactor.*;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.*;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.logic.AndPredicate;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.logic.NotPredicate;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.logic.OrPredicate;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.logic.XorPredicate;
import com.github.phantazmnetwork.zombies.game.perk.ExtraHealthLevel;
import com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.lineupdater.*;
import com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.lineupdater.condition.AliveCondition;
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
        registerElementClasses(contextManager);
    }

    private static void registerElementClasses(ContextManager contextManager) {
        LOGGER.info("Registering Zombies element classes...");
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

        contextManager.registerElementClass(EquipmentCostPredicate.class);
        contextManager.registerElementClass(EquipmentSpacePredicate.class);

        //ShopInteractor
        contextManager.registerElementClass(FlagSettingInteractor.class);
        contextManager.registerElementClass(MessagingInteractor.class);
        contextManager.registerElementClass(PlaySoundInteractor.class);
        contextManager.registerElementClass(DelayedInteractor.class);
        contextManager.registerElementClass(ConditionalInteractor.class);
        contextManager.registerElementClass(OpenGuiInteractor.class);
        contextManager.registerElementClass(CloseGuiInteractor.class);
        contextManager.registerElementClass(AddEquipmentInteractor.class);
        contextManager.registerElementClass(ChangeDoorStateInteractor.class);

        //ShopDisplay
        contextManager.registerElementClass(StaticHologramDisplay.class);
        contextManager.registerElementClass(StaticItemDisplay.class);
        contextManager.registerElementClass(ConditionalDisplay.class);
        contextManager.registerElementClass(IncrementalMetaDisplay.class);
        contextManager.registerElementClass(AnimatedItemDisplay.class);

        //Sidebar LineUpdaters
        contextManager.registerElementClass(CoinsSidebarLineUpdater.class);
        contextManager.registerElementClass(ConditionalSidebarLineUpdater.class);
        contextManager.registerElementClass(ConditionalSidebarLineUpdater.ChildUpdater.class);
        contextManager.registerElementClass(AliveCondition.class);
        contextManager.registerElementClass(ConstantSidebarLineUpdater.class);
        contextManager.registerElementClass(JoinedPlayersSidebarLineUpdater.class);
        contextManager.registerElementClass(PlayerStateSidebarLineUpdater.class);
        contextManager.registerElementClass(RemainingZombiesSidebarLineUpdater.class);
        contextManager.registerElementClass(RoundSidebarLineUpdater.class);
        contextManager.registerElementClass(TicksLineUpdater.class);
        contextManager.registerElementClass(ZombieKillsSidebarLineUpdater.class);

        //ClickHandlers
        contextManager.registerElementClass(InteractingClickHandler.class);

        //UpdatingItem
        contextManager.registerElementClass(StaticUpdatingItem.class);
        contextManager.registerElementClass(AnimatedUpdatingItem.class);

        //UpgradePath
        contextManager.registerElementClass(LinearUpgradePath.class);

        //PerkLevels
        contextManager.registerElementClass(ExtraHealthLevel.class);

        LOGGER.info("Registered Zombies element classes.");
    }

    public static @NotNull ContextManager mapObjectBuilder() {
        if (contextManager == null) {
            throw new IllegalStateException("ZombiesFeature has not been initialized yet");
        }

        return contextManager;
    }
}
