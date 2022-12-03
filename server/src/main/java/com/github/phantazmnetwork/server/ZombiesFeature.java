package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.commons.FileUtils;
import com.github.phantazmnetwork.core.item.AnimatedUpdatingItem;
import com.github.phantazmnetwork.core.item.StaticUpdatingItem;
import com.github.phantazmnetwork.zombies.map.FileSystemMapLoader;
import com.github.phantazmnetwork.zombies.map.MapInfo;
import com.github.phantazmnetwork.zombies.map.MapLoader;
import com.github.phantazmnetwork.zombies.map.action.room.SpawnMobsAction;
import com.github.phantazmnetwork.zombies.map.action.round.AnnounceRoundAction;
import com.github.phantazmnetwork.zombies.map.action.round.RevivePlayersAction;
import com.github.phantazmnetwork.zombies.map.shop.LinearUpgradePath;
import com.github.phantazmnetwork.zombies.map.shop.display.*;
import com.github.phantazmnetwork.zombies.map.shop.gui.InteractingClickHandler;
import com.github.phantazmnetwork.zombies.map.shop.interactor.*;
import com.github.phantazmnetwork.zombies.map.shop.predicate.*;
import com.github.phantazmnetwork.zombies.map.shop.predicate.logic.AndPredicate;
import com.github.phantazmnetwork.zombies.map.shop.predicate.logic.NotPredicate;
import com.github.phantazmnetwork.zombies.map.shop.predicate.logic.OrPredicate;
import com.github.phantazmnetwork.zombies.map.shop.predicate.logic.XorPredicate;
import com.github.phantazmnetwork.zombies.perk.ExtraHealthLevel;
import com.github.phantazmnetwork.zombies.powerup.*;
import com.github.phantazmnetwork.zombies.scoreboard.sidebar.SidebarUpdater;
import com.github.phantazmnetwork.zombies.scoreboard.sidebar.lineupdater.*;
import com.github.phantazmnetwork.zombies.scoreboard.sidebar.lineupdater.condition.AliveCondition;
import com.github.phantazmnetwork.zombies.scoreboard.sidebar.section.CollectionSidebarSection;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.ethylene.codec.yaml.YamlCodec;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ZombiesFeature {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesFeature.class);
    private static final Map<Key, MapInfo> maps = new HashMap<>();

    private static ContextManager contextManager;

    static void initialize(@NotNull ContextManager contextManager) throws IOException {
        ZombiesFeature.contextManager = Objects.requireNonNull(contextManager, "contextManager");
        registerElementClasses(contextManager);
        loadMaps();
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
        contextManager.registerElementClass(DeductCoinsInteractor.class);

        //ShopDisplay
        contextManager.registerElementClass(StaticHologramDisplay.class);
        contextManager.registerElementClass(StaticItemDisplay.class);
        contextManager.registerElementClass(ConditionalDisplay.class);
        contextManager.registerElementClass(IncrementalMetaDisplay.class);
        contextManager.registerElementClass(AnimatedItemDisplay.class);

        //Sidebar
        contextManager.registerElementClass(SidebarUpdater.class);
        contextManager.registerElementClass(CollectionSidebarSection.class);
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

        //DeactivationPredicates
        contextManager.registerElementClass(TimedDeactivationPredicate.class);
        contextManager.registerElementClass(ImmediateDeactivationPredicate.class);

        //PowerupVisuals
        contextManager.registerElementClass(ItemVisual.class);
        contextManager.registerElementClass(HologramVisual.class);

        //PowerupActions
        contextManager.registerElementClass(MapFlaggingAction.class);
        contextManager.registerElementClass(PlayerFlaggingAction.class);
        contextManager.registerElementClass(MapTransactionModifierMultiplyAction.class);
        contextManager.registerElementClass(MapTransactionModifierAddAction.class);

        LOGGER.info("Registered Zombies element classes.");
    }

    private static void loadMaps() throws IOException {
        Path rootFolder = Path.of("./zombies/maps/");
        MapLoader mapLoader = new FileSystemMapLoader(rootFolder, new YamlCodec());

        Files.createDirectories(rootFolder);
        FileUtils.forEachFileMatching(rootFolder, (path, attr) -> attr.isDirectory() && !path.equals(rootFolder),
                mapFolder -> {
                    LOGGER.info("Trying to load map from " + mapFolder);
                    String name = mapFolder.getFileName().toString();

                    try {
                        MapInfo map = mapLoader.load(name);
                        maps.put(map.settings().id(), map);
                        LOGGER.info("Successfully loaded map " + name);
                    }
                    catch (IOException e) {
                        LOGGER.warn("IOException when loading map " + name, e);
                    }
                });
    }

    public static @NotNull Map<Key, MapInfo> maps() {
        return maps;
    }

    public static @NotNull ContextManager mapObjectBuilder() {
        return FeatureUtils.check(contextManager);
    }
}
