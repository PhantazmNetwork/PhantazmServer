package org.phantazm.zombies.player;

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
import org.phantazm.commons.Namespaces;
import org.phantazm.core.entity.fakeplayer.MinimalFakePlayer;
import org.phantazm.core.hologram.Hologram;
import org.phantazm.core.hologram.InstanceHologram;
import org.phantazm.core.inventory.*;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.time.DurationTickFormatter;
import org.phantazm.core.time.TickFormatter;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.spawner.MobSpawner;
import org.phantazm.zombies.coin.BasicPlayerCoins;
import org.phantazm.zombies.coin.BasicTransactionModifierSource;
import org.phantazm.zombies.coin.PlayerCoins;
import org.phantazm.zombies.coin.TransactionModifierSource;
import org.phantazm.zombies.coin.component.BasicTransactionComponentCreator;
import org.phantazm.zombies.corpse.Corpse;
import org.phantazm.zombies.equipment.EquipmentCreator;
import org.phantazm.zombies.equipment.EquipmentHandler;
import org.phantazm.zombies.equipment.gun.ZombiesGunModule;
import org.phantazm.zombies.kill.BasicPlayerKills;
import org.phantazm.zombies.kill.PlayerKills;
import org.phantazm.zombies.map.Flaggable;
import org.phantazm.zombies.map.MapSettingsInfo;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.state.*;
import org.phantazm.zombies.player.state.context.DeadPlayerStateContext;
import org.phantazm.zombies.player.state.context.KnockedPlayerStateContext;
import org.phantazm.zombies.player.state.context.NoContext;
import org.phantazm.zombies.player.state.revive.BasicKnockedStateActivable;
import org.phantazm.zombies.player.state.revive.KnockedPlayerState;
import org.phantazm.zombies.player.state.revive.NearbyReviverFinder;
import org.phantazm.zombies.player.state.revive.ReviveHandler;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class BasicZombiesPlayerSource implements ZombiesPlayer.Source {
    private final Function<ZombiesGunModule, EquipmentCreator> equipmentCreatorFunction;

    private final Team corpseTeam;

    public BasicZombiesPlayerSource(@NotNull Function<ZombiesGunModule, EquipmentCreator> equipmentCreatorFunction,
            @NotNull Team corpseTeam) {
        this.equipmentCreatorFunction = Objects.requireNonNull(equipmentCreatorFunction, "equipmentCreatorFunction");
        this.corpseTeam = Objects.requireNonNull(corpseTeam, "corpseTeam");
    }

    @Override
    public @NotNull ZombiesPlayer createPlayer(@NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers,
            @NotNull MapSettingsInfo mapSettingsInfo, @NotNull Instance instance, @NotNull PlayerView playerView,
            @NotNull TransactionModifierSource mapTransactionModifierSource, @NotNull Flaggable flaggable,
            @NotNull EventNode<Event> eventNode, @NotNull Random random, @NotNull MapObjects mapObjects,
            @NotNull MobStore mobStore, @NotNull MobSpawner mobSpawner) {
        TransactionModifierSource playerTransactionModifierSource = new BasicTransactionModifierSource();

        ZombiesPlayerMeta meta = new ZombiesPlayerMeta();
        PlayerCoins coins = new BasicPlayerCoins(playerView, new BasicTransactionComponentCreator(), 0);
        PlayerKills kills = new BasicPlayerKills();
        InventoryProfile profile = new BasicInventoryProfile(9);
        InventoryAccess inventoryAccess = new InventoryAccess(profile,
                Map.of(Key.key(Namespaces.PHANTAZM, "inventory.group.gun"),
                        new BasicInventoryObjectGroup(profile, IntSet.of(1, 2))));
        InventoryAccessRegistry accessRegistry = new BasicInventoryAccessRegistry();
        Key accessKey = Key.key(Namespaces.PHANTAZM, "inventory.access.default");
        accessRegistry.registerAccess(accessKey, inventoryAccess);
        accessRegistry.switchAccess(accessKey);
        EquipmentHandler equipmentHandler = new EquipmentHandler(accessRegistry);

        ZombiesGunModule gunModule =
                new ZombiesGunModule(zombiesPlayers, playerView, mobSpawner, mobStore, eventNode, random, mapObjects);
        EquipmentCreator equipmentCreator = equipmentCreatorFunction.apply(gunModule);

        Sidebar sidebar = new Sidebar(
                Component.text(StringUtils.center("ZOMBIES", 16), NamedTextColor.YELLOW, TextDecoration.BOLD));

        Function<NoContext, ZombiesPlayerState> aliveStateCreator = unused -> {
            return new BasicZombiesPlayerState(Component.text("ALIVE", NamedTextColor.GREEN),
                    ZombiesPlayerStateKeys.ALIVE.key(),
                    List.of(new BasicAliveStateActivable(playerView, meta, sidebar)));
        };
        BiFunction<DeadPlayerStateContext, Collection<Activable>, ZombiesPlayerState> deadStateCreator =
                (context, activables) -> {
                    List<Activable> combinedActivables = new ArrayList<>(activables);
                    combinedActivables.add(new BasicDeadStateActivable(context, instance, playerView, meta, sidebar));
                    return new BasicZombiesPlayerState(Component.text("DEAD", NamedTextColor.RED),
                            ZombiesPlayerStateKeys.DEAD.key(), combinedActivables);
                };
        Function<KnockedPlayerStateContext, ZombiesPlayerState> knockedStateCreator = context -> {
            Hologram hologram =
                    new InstanceHologram(context.getKnockLocation().add(0, 0.5, 0), 0, Hologram.Alignment.LOWER);
            hologram.setInstance(instance);

            PlayerSkin skin = playerView.getPlayer().map(Player::getSkin).orElse(null);
            String corpseUsername = UUID.randomUUID().toString().substring(0, 16);
            Entity corpseEntity = new MinimalFakePlayer(MinecraftServer.getSchedulerManager(), corpseUsername, skin);

            corpseEntity.setInstance(instance, context.getKnockLocation().add(0, 0.25, 0));
            corpseTeam.addMember(corpseUsername);
            TickFormatter tickFormatter = new DurationTickFormatter(NamedTextColor.RED, false, false);
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
            return new BasicZombiesPlayerState(Component.text("QUIT", NamedTextColor.RED),
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

        return new BasicZombiesPlayer(module);
    }
}
