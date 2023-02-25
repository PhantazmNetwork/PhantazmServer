package org.phantazm.zombies.player;

import com.github.steanky.toolkit.collection.Wrapper;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.*;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.Instance;
import net.minestom.server.scoreboard.Sidebar;
import net.minestom.server.scoreboard.Team;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Activable;
import org.phantazm.core.entity.fakeplayer.MinimalFakePlayer;
import org.phantazm.core.hologram.Hologram;
import org.phantazm.core.hologram.InstanceHologram;
import org.phantazm.core.inventory.*;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.time.PrecisionSecondTickFormatter;
import org.phantazm.core.time.TickFormatter;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.spawner.MobSpawner;
import org.phantazm.zombies.coin.BasicPlayerCoins;
import org.phantazm.zombies.coin.BasicTransactionModifierSource;
import org.phantazm.zombies.coin.PlayerCoins;
import org.phantazm.zombies.coin.TransactionModifierSource;
import org.phantazm.zombies.coin.component.BasicTransactionComponentCreator;
import org.phantazm.zombies.corpse.Corpse;
import org.phantazm.core.equipment.EquipmentCreator;
import org.phantazm.core.equipment.EquipmentHandler;
import org.phantazm.zombies.equipment.gun.ZombiesEquipmentModule;
import org.phantazm.zombies.kill.BasicPlayerKills;
import org.phantazm.zombies.kill.PlayerKills;
import org.phantazm.zombies.map.Flaggable;
import org.phantazm.zombies.map.MapSettingsInfo;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.state.*;
import org.phantazm.zombies.player.state.context.DeadPlayerStateContext;
import org.phantazm.zombies.player.state.context.KnockedPlayerStateContext;
import org.phantazm.zombies.player.state.context.NoContext;
import org.phantazm.zombies.player.state.BasicKnockedStateActivable;
import org.phantazm.zombies.player.state.revive.KnockedPlayerState;
import org.phantazm.zombies.player.state.revive.NearbyReviverFinder;
import org.phantazm.zombies.player.state.revive.ReviveHandler;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class BasicZombiesPlayerSource implements ZombiesPlayer.Source {
    private final Function<ZombiesEquipmentModule, EquipmentCreator> equipmentCreatorFunction;

    private final Team corpseTeam;

    public BasicZombiesPlayerSource(
            @NotNull Function<ZombiesEquipmentModule, EquipmentCreator> equipmentCreatorFunction,
            @NotNull Team corpseTeam) {
        this.equipmentCreatorFunction = Objects.requireNonNull(equipmentCreatorFunction, "equipmentCreatorFunction");
        this.corpseTeam = Objects.requireNonNull(corpseTeam, "corpseTeam");
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull ZombiesPlayer createPlayer(@NotNull ZombiesScene scene,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers,
            @NotNull MapSettingsInfo mapSettingsInfo, @NotNull Instance instance, @NotNull PlayerView playerView,
            @NotNull TransactionModifierSource mapTransactionModifierSource, @NotNull Flaggable flaggable,
            @NotNull EventNode<Event> eventNode, @NotNull Random random, @NotNull MapObjects mapObjects,
            @NotNull MobStore mobStore, @NotNull MobSpawner mobSpawner) {
        TransactionModifierSource playerTransactionModifierSource = new BasicTransactionModifierSource();

        ZombiesPlayerMeta meta = new ZombiesPlayerMeta();
        meta.setTicksPerHeal(mapSettingsInfo.healTicks());

        PlayerCoins coins = new BasicPlayerCoins(playerView, new BasicTransactionComponentCreator(), 0);
        PlayerKills kills = new BasicPlayerKills();

        InventoryProfile livingProfile = new BasicInventoryProfile(9);

        Map<Key, IntSet> equipmentGroups = mapSettingsInfo.equipmentGroups();

        Map.Entry<Key, InventoryObjectGroup>[] inventoryObjectGroupEntries = new Map.Entry[equipmentGroups.size()];
        Iterator<Map.Entry<Key, IntSet>> iterator = equipmentGroups.entrySet().iterator();
        for (int i = 0; i < inventoryObjectGroupEntries.length; i++) {
            Map.Entry<Key, IntSet> entry = iterator.next();
            inventoryObjectGroupEntries[i] =
                    Map.entry(entry.getKey(), new BasicInventoryObjectGroup(livingProfile, entry.getValue()));
        }

        InventoryAccess livingInventoryAccess =
                new InventoryAccess(livingProfile, Map.ofEntries(inventoryObjectGroupEntries));
        InventoryAccess deadInventoryAccess = new InventoryAccess(new BasicInventoryProfile(9), Map.of());

        InventoryAccessRegistry accessRegistry = new BasicInventoryAccessRegistry();
        accessRegistry.registerAccess(InventoryKeys.DEFAULT_ACCESS, livingInventoryAccess);
        accessRegistry.registerAccess(InventoryKeys.DEAD_ACCESS, deadInventoryAccess);

        EquipmentHandler equipmentHandler = new EquipmentHandler(accessRegistry);

        Wrapper<ZombiesPlayer> zombiesPlayerWrapper = Wrapper.ofNull();
        ZombiesEquipmentModule equipmentModule =
                new ZombiesEquipmentModule(zombiesPlayers, playerView, mobSpawner, mobStore, eventNode, random,
                        mapObjects, zombiesPlayerWrapper);
        EquipmentCreator equipmentCreator = equipmentCreatorFunction.apply(equipmentModule);

        Sidebar sidebar = new Sidebar(
                Component.text(StringUtils.center("ZOMBIES", 16), NamedTextColor.YELLOW, TextDecoration.BOLD));

        Function<NoContext, ZombiesPlayerState> aliveStateCreator = unused -> {
            return new BasicZombiesPlayerState(Component.text("ALIVE"), ZombiesPlayerStateKeys.ALIVE.key(),
                    List.of(new BasicAliveStateActivable(accessRegistry, playerView, meta, sidebar)));
        };
        BiFunction<DeadPlayerStateContext, Collection<Activable>, ZombiesPlayerState> deadStateCreator =
                (context, activables) -> {
                    List<Activable> combinedActivables = new ArrayList<>(activables);
                    combinedActivables.add(
                            new BasicDeadStateActivable(accessRegistry, context, instance, playerView, meta, sidebar));
                    return new BasicZombiesPlayerState(Component.text("DEAD").color(NamedTextColor.RED),
                            ZombiesPlayerStateKeys.DEAD.key(), combinedActivables);
                };
        Function<KnockedPlayerStateContext, ZombiesPlayerState> knockedStateCreator = context -> {
            Hologram hologram =
                    new InstanceHologram(context.getKnockLocation().add(0, 0.5, 0), 0, Hologram.Alignment.LOWER);
            hologram.setInstance(instance);

            PlayerSkin skin = playerView.getPlayer().map(Player::getSkin).orElse(null);
            String corpseUsername = UUID.randomUUID().toString().substring(0, 16);
            MinimalFakePlayer corpseEntity =
                    new MinimalFakePlayer(MinecraftServer.getSchedulerManager(), corpseUsername, skin);

            corpseEntity.setInstance(instance, context.getKnockLocation().add(0, 0.25, 0));
            corpseTeam.addMember(corpseUsername);
            TickFormatter tickFormatter =
                    new PrecisionSecondTickFormatter(new PrecisionSecondTickFormatter.Data(NamedTextColor.RED, 1));
            Corpse corpse = new Corpse(hologram, corpseEntity, tickFormatter);

            Supplier<ZombiesPlayerState> deadStateSupplier = () -> {
                DeadPlayerStateContext deathContext = DeadPlayerStateContext.killed(context.getKiller().orElse(null),
                        context.getKnockRoom().orElse(null));
                return deadStateCreator.apply(deathContext, List.of(corpse.asDeathActivable(), new Activable() {
                    @Override
                    public void end() {
                        meta.setCorpse(null);
                    }
                }));
            };

            ReviveHandler reviveHandler =
                    new ReviveHandler(() -> aliveStateCreator.apply(NoContext.INSTANCE), deadStateSupplier,
                            new NearbyReviverFinder(zombiesPlayers, playerView, mapSettingsInfo.reviveRadius()), 500L);

            return new KnockedPlayerState(reviveHandler,
                    List.of(new BasicKnockedStateActivable(context, instance, playerView, reviveHandler, tickFormatter,
                            meta, sidebar), corpse.asKnockActivable(reviveHandler), new Activable() {
                        @Override
                        public void start() {
                            meta.setCorpse(corpse);
                        }
                    }));
        };
        Function<NoContext, ZombiesPlayerState> quitStateCreator = unused -> {
            return new BasicZombiesPlayerState(Component.text("QUIT").color(NamedTextColor.RED),
                    ZombiesPlayerStateKeys.QUIT.key(),
                    List.of(new BasicQuitStateActivable(instance, playerView, meta, sidebar)));
        };
        PlayerStateSwitcher stateSwitcher = new PlayerStateSwitcher(aliveStateCreator.apply(NoContext.INSTANCE));
        Map<PlayerStateKey<?>, Function<?, ? extends ZombiesPlayerState>> stateFunctions =
                Map.of(ZombiesPlayerStateKeys.ALIVE, aliveStateCreator, ZombiesPlayerStateKeys.DEAD,
                        (Function<DeadPlayerStateContext, ZombiesPlayerState>)context -> deadStateCreator.apply(context,
                                List.of()), ZombiesPlayerStateKeys.KNOCKED, knockedStateCreator,
                        ZombiesPlayerStateKeys.QUIT, quitStateCreator);

        ZombiesPlayerModule module =
                new ZombiesPlayerModule(playerView, meta, coins, kills, equipmentHandler, equipmentCreator,
                        accessRegistry, stateSwitcher, stateFunctions, sidebar, mapTransactionModifierSource,
                        playerTransactionModifierSource, flaggable);

        ZombiesPlayer zombiesPlayer = new BasicZombiesPlayer(scene, module);
        zombiesPlayerWrapper.set(zombiesPlayer);
        return zombiesPlayer;
    }
}
