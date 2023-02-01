package org.phantazm.server;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.ethylene.codec.yaml.YamlCodec;
import com.github.steanky.ethylene.core.ConfigCodec;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.item.AnimatedUpdatingItem;
import org.phantazm.core.item.StaticUpdatingItem;
import org.phantazm.zombies.map.FileSystemMapLoader;
import org.phantazm.zombies.map.Loader;
import org.phantazm.zombies.map.MapInfo;
import org.phantazm.zombies.map.action.door.DoorSendMessageAction;
import org.phantazm.zombies.map.action.door.DoorSendOpenedRoomsAction;
import org.phantazm.zombies.map.action.room.SpawnMobsAction;
import org.phantazm.zombies.map.action.round.AnnounceRoundAction;
import org.phantazm.zombies.map.action.round.RevivePlayersAction;
import org.phantazm.zombies.map.action.round.SpawnPowerupAction;
import org.phantazm.zombies.map.shop.LinearUpgradePath;
import org.phantazm.zombies.map.shop.display.*;
import org.phantazm.zombies.map.shop.gui.InteractingClickHandler;
import org.phantazm.zombies.map.shop.interactor.*;
import org.phantazm.zombies.map.shop.predicate.*;
import org.phantazm.zombies.map.shop.predicate.logic.AndPredicate;
import org.phantazm.zombies.map.shop.predicate.logic.NotPredicate;
import org.phantazm.zombies.map.shop.predicate.logic.OrPredicate;
import org.phantazm.zombies.map.shop.predicate.logic.XorPredicate;
import org.phantazm.zombies.perk.*;
import org.phantazm.zombies.powerup.FileSystemPowerupLoader;
import org.phantazm.zombies.powerup.PowerupInfo;
import org.phantazm.zombies.powerup.action.*;
import org.phantazm.zombies.powerup.predicate.ImmediateDeactivationPredicate;
import org.phantazm.zombies.powerup.predicate.TimedDeactivationPredicate;
import org.phantazm.zombies.powerup.visual.HologramVisual;
import org.phantazm.zombies.powerup.visual.ItemVisual;
import org.phantazm.zombies.scoreboard.sidebar.SidebarUpdater;
import org.phantazm.zombies.scoreboard.sidebar.lineupdater.*;
import org.phantazm.zombies.scoreboard.sidebar.lineupdater.condition.AliveCondition;
import org.phantazm.zombies.scoreboard.sidebar.section.CollectionSidebarSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ZombiesFeature {
    public static final Path MAPS_FOLDER = Path.of("./zombies/maps");
    public static final Path POWERUPS_FOLDER = Path.of("./zombies/powerups");

    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesFeature.class);

    private static Map<Key, MapInfo> maps;
    private static Map<Key, PowerupInfo> powerups;
    private static ContextManager contextManager;

    static void initialize(@NotNull ContextManager contextManager) throws IOException {
        ZombiesFeature.contextManager = Objects.requireNonNull(contextManager, "contextManager");
        registerElementClasses(contextManager);

        ConfigCodec codec = new YamlCodec();
        ZombiesFeature.maps = loadFeature("map", new FileSystemMapLoader(MAPS_FOLDER, codec));
        ZombiesFeature.powerups = loadFeature("powerup", new FileSystemPowerupLoader(POWERUPS_FOLDER, codec));
    }

    private static void registerElementClasses(ContextManager contextManager) {
        LOGGER.info("Registering Zombies element classes...");
        //Action<Room>, Action<Round> and Action<Door>
        contextManager.registerElementClass(AnnounceRoundAction.class);
        contextManager.registerElementClass(RevivePlayersAction.class);
        contextManager.registerElementClass(SpawnMobsAction.class);
        contextManager.registerElementClass(SpawnPowerupAction.class);
        contextManager.registerElementClass(DoorSendMessageAction.class);
        contextManager.registerElementClass(DoorSendOpenedRoomsAction.class);

        //ShopPredicate
        contextManager.registerElementClass(StaticCostPredicate.class);
        contextManager.registerElementClass(MapFlagPredicate.class);
        contextManager.registerElementClass(PlayerFlagPredicate.class);
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
        contextManager.registerElementClass(MapFlaggingInteractor.class);
        contextManager.registerElementClass(PlayerFlaggingInteractor.class);
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
        contextManager.registerElementClass(AddShotHandlerLevel.Creator.class);
        contextManager.registerElementClass(ExtraHealthLevel.Creator.class);
        contextManager.registerElementClass(ExtraWeaponLevel.Creator.class);
        contextManager.registerElementClass(FastReviveLevel.Creator.class);
        contextManager.registerElementClass(QuickFireLevel.Creator.class);
        contextManager.registerElementClass(SpeedLevel.Creator.class);

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
        contextManager.registerElementClass(KillAllInRadiusAction.class);
        contextManager.registerElementClass(PlaySoundAction.class);
        contextManager.registerElementClass(ModifyWindowsAction.class);
        contextManager.registerElementClass(SendTitleAction.class);
        contextManager.registerElementClass(PowerupSendMessageAction.class);

        LOGGER.info("Registered Zombies element classes.");
    }

    private static <T extends Keyed> Map<Key, T> loadFeature(String featureName, Loader<T> loader) throws IOException {
        LOGGER.info("Loading " + featureName + "s...");

        List<String> dataNames = loader.loadableData();
        Map<Key, T> data = new HashMap<>(dataNames.size());

        for (String dataName : dataNames) {
            LOGGER.info("Trying to load " + featureName + " from " + dataName);

            try {
                T feature = loader.load(dataName);
                Key id = feature.key();

                if (data.containsKey(id)) {
                    LOGGER.warn("Found duplicate " + featureName + " with id " + id + "; the previously loaded " +
                            "version will be overwritten");
                }

                data.put(id, feature);
                LOGGER.info("Successfully loaded " + featureName + " " + id);
            }
            catch (IOException e) {
                LOGGER.warn("Exception when loading " + featureName, e);
            }
        }

        LOGGER.info("Loaded " + data.size() + " " + featureName + "s");
        return Map.copyOf(data);
    }

    public static @NotNull @Unmodifiable Map<Key, MapInfo> maps() {
        return FeatureUtils.check(maps);
    }

    public static @NotNull @Unmodifiable Map<Key, PowerupInfo> powerups() {
        return FeatureUtils.check(powerups);
    }

    public static @NotNull ContextManager mapObjectBuilder() {
        return FeatureUtils.check(contextManager);
    }
}
