package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.api.chat.ChatChannelSendEvent;
import com.github.phantazmnetwork.api.inventory.*;
import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.api.player.PlayerViewProvider;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.config.ComplexData;
import com.github.phantazmnetwork.mob.MobModel;
import com.github.phantazmnetwork.mob.goal.FollowPlayerGoal;
import com.github.phantazmnetwork.mob.target.FirstTargetSelector;
import com.github.phantazmnetwork.mob.target.NearestPlayersSelector;
import com.github.phantazmnetwork.mob.target.TargetSelector;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.GroundMinestomDescriptor;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.equipment.gun.*;
import com.github.phantazmnetwork.zombies.equipment.gun.audience.EntityInstanceAudienceProvider;
import com.github.phantazmnetwork.zombies.equipment.gun.data.GunLevelData;
import com.github.phantazmnetwork.zombies.equipment.gun.effect.PlaySoundEffect;
import com.github.phantazmnetwork.zombies.equipment.gun.effect.ReloadActionBarEffect;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.StateReloadTester;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.actionbar.GradientActionBarChooser;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.StateShootTester;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint.BasicShotEndpointSelector;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint.RayTraceBlockIteration;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.HitScanFirer;
import com.github.phantazmnetwork.zombies.equipment.gun.target.BasicTargetFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.directional.BetweenPointsFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.headshot.EyeHeightHeadshotTester;
import com.github.phantazmnetwork.zombies.equipment.gun.target.intersectionfinder.RayTraceIntersectionFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.limiter.DistanceTargetLimiter;
import com.github.phantazmnetwork.zombies.equipment.gun.target.tester.PhantazmTargetTester;
import com.github.phantazmnetwork.zombies.equipment.gun.visual.ClipStackMapper;
import com.github.steanky.ethylene.codec.yaml.YamlCodec;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.bridge.ConfigBridges;
import com.github.steanky.ethylene.core.codec.ConfigCodec;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.PlayerHeadMeta;
import org.jetbrains.annotations.NotNull;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

final class GunTest {

    private GunTest() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("UnstableApiUsage")
    static void initialize(@NotNull EventNode<Event> global, @NotNull EventNode<? super ChatChannelSendEvent> phantazm,
                           @NotNull PlayerViewProvider viewProvider) {
        Map<UUID, InventoryProfileSwitcher> profileSwitchers = new HashMap<>();
        Key defaultProfileKey = Key.key(Namespaces.PHANTAZM, "inventory.profile.default");
        global.addListener(PlayerSpawnEvent.class, event -> {
            InventoryProfile profile = profileSwitchers.computeIfAbsent(event.getPlayer().getUuid(), unused -> {
                BasicInventoryProfileSwitcher switcher = new BasicInventoryProfileSwitcher();
                switcher.registerProfile(defaultProfileKey, new BasicInventoryProfile(9));
                switcher.switchProfile(defaultProfileKey);
                return switcher;
            }).getCurrentProfile();
            for (int i = 0; i < 9; i++) {
                if (profile.hasInventoryObject(i)) {
                    event.getPlayer().getInventory().setItemStack(i, profile.getInventoryObject(i).getItemStack());
                }
            }
        });
        global.addListener(PlayerUseItemEvent.class, event -> {
            InventoryProfile profile = profileSwitchers.get(event.getPlayer().getUuid()).getCurrentProfile();
            if (profile.hasInventoryObject(event.getPlayer().getHeldSlot())) {
                InventoryObject object = profile.getInventoryObject(event.getPlayer().getHeldSlot());
                if (object instanceof Equipment equipment) {
                    equipment.rightClick();
                }
            }
        });
        global.addListener(PlayerChangeHeldSlotEvent.class, event -> {
            InventoryProfile profile = profileSwitchers.get(event.getPlayer().getUuid()).getCurrentProfile();
            if (profile.hasInventoryObject(event.getPlayer().getHeldSlot())) {
                InventoryObject oldObject = profile.getInventoryObject(event.getPlayer().getHeldSlot());
                if (oldObject instanceof Equipment equipment) {
                    equipment.setSelected(false);
                }
            }
            if (profile.hasInventoryObject(event.getSlot())) {
                InventoryObject newObject = profile.getInventoryObject(event.getSlot());
                if (newObject instanceof Equipment equipment) {
                    equipment.setSelected(true);
                }
            }
        });
        global.addListener(PlayerHandAnimationEvent.class, event -> {
            InventoryProfile profile = profileSwitchers.get(event.getPlayer().getUuid()).getCurrentProfile();
            if (profile.hasInventoryObject(event.getPlayer().getHeldSlot())) {
                InventoryObject object = profile.getInventoryObject(event.getPlayer().getHeldSlot());
                if (object instanceof Equipment equipment) {
                    equipment.leftClick();
                }
            }
        });
        global.addListener(InstanceTickEvent.class, event -> {
           for (Player player : event.getInstance().getPlayers()) {
               InventoryProfile profile = profileSwitchers.get(player.getUuid()).getCurrentProfile();
               for (int slot = 0; slot < 9; slot++) {
                   if (profile.hasInventoryObject(slot)) {
                       InventoryObject object = profile.getInventoryObject(slot);
                       if (object.shouldRedraw()) {
                           player.getInventory().setItemStack(slot, object.getItemStack());
                       }
                       object.tick(System.currentTimeMillis());
                   }
               }
           }
        });
        phantazm.addListener(ChatChannelSendEvent.class, event -> {
            Instance instance = event.getPlayer().getInstance();
            if (instance == null) {
                return;
            }

            String msg = event.getInput();
            switch (msg) {
                case "guntest" -> {
                    PlayerView view = viewProvider.fromPlayer(event.getPlayer());
                    Key gunKey = Key.key(Namespaces.PHANTAZM, "test_gun");
                    Gun gun = EquipmentFeature.createGun(gunKey, global, Mob.getMobStore(), view, new Random());

                    profileSwitchers.get(event.getPlayer().getUuid()).getCurrentProfile().setInventoryObject(0, gun);
                    event.getPlayer().getInventory().setItemStack(0, gun.getItemStack());
                    if (event.getPlayer().getHeldSlot() == 0) {
                        gun.setSelected(true);
                    }
                }
                case "va" -> {
                    ItemStack head = ItemStack.builder(Material.PLAYER_HEAD)
                            .meta(new PlayerHeadMeta.Builder()
                                    .skullOwner(UUID.fromString("0a824693-c978-49be-a7a3-cef87e8acb8e"))
                                    .playerSkin(PlayerSkin.fromUsername("VeryAveragesAlt"))
                                    .build()).build();
                    TargetSelector<Player> playerSelector = new FirstTargetSelector<>(new NearestPlayersSelector(20.0F, 1));
                    MobModel mobModel = new MobModel(
                            Key.key(Namespaces.PHANTAZM, "mob.test.zombie"),
                            GroundMinestomDescriptor.of(EntityType.ZOMBIE, "test"),
                            Collections.singleton(Collections.singleton(new FollowPlayerGoal(playerSelector))),
                            Collections.emptyMap(),
                            Component.text("VeryAverage", NamedTextColor.RED),
                            Map.of(
                                    EquipmentSlot.HELMET, head
                            ),
                            new Object2FloatOpenHashMap<>(2) {
                                {
                                    put(Attribute.MAX_HEALTH.key(), Attribute.MAX_HEALTH.defaultValue());
                                    put(Attribute.MOVEMENT_SPEED.key(),
                                            Attribute.MOVEMENT_SPEED.defaultValue() / 3.0F);
                                }
                            }
                    );
                    Mob.getMobSpawner().spawn(instance, event.getPlayer().getPosition(), mobModel);
                }
                case "max ammo" -> {
                    Player player = event.getPlayer();
                    InventoryProfile profile = profileSwitchers.get(player.getUuid()).getCurrentProfile();
                    if (profile.hasInventoryObject(player.getHeldSlot())) {
                        InventoryObject object = profile.getInventoryObject(player.getHeldSlot());
                        if (object instanceof Gun gun) {
                            gun.refill();
                        }
                    }
                }
            }
        });

        Map<Key, ConfigProcessor<? extends Keyed>> subprocessors = new HashMap<>();
        subprocessors.put(GunStats.SERIAL_KEY, new ConfigProcessor<GunStats>() {

            @Override
            public @NotNull GunStats dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                long shootSpeed = element.getNumberOrThrow("shootSpeed").longValue();
                long reloadSpeed = element.getNumberOrThrow("reloadSpeed").longValue();
                int maxAmmo = element.getNumberOrThrow("maxAmmo").intValue();
                int maxClip = element.getNumberOrThrow("maxClip").intValue();
                int shots = element.getNumberOrThrow("shots").intValue();
                long shotInterval = element.getNumberOrThrow("shotInterval").longValue();

                return new GunStats(shootSpeed, reloadSpeed, maxAmmo, maxClip, shots, shotInterval);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull GunStats stats) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(6);
                node.putNumber("shootSpeed", stats.shootSpeed());
                node.putNumber("reloadSpeed", stats.reloadSpeed());
                node.putNumber("maxAmmo", stats.maxAmmo());
                node.putNumber("maxClip", stats.maxClip());
                node.putNumber("shots", stats.shots());
                node.putNumber("shotInterval", stats.shotInterval());

                return node;
            }
        });
        ConfigProcessor<ComplexData> cfg = EquipmentFeature.createGunLevelProcessor();

        Key sStatsKey = Key.key(Namespaces.PHANTAZM, "stats");
        GunStats sStats = new GunStats(10L, 30L, 300, 10, 1, 0L);
        Key sReloadTesterKey = Key.key(Namespaces.PHANTAZM, "reload_tester");
        StateReloadTester.Data sReloadTester = new StateReloadTester.Data(sStatsKey);
        Key sShootTesterKey = Key.key(Namespaces.PHANTAZM, "shoot_tester");
        StateShootTester.Data sShootTester = new StateShootTester.Data(sStatsKey, sReloadTesterKey);
        Key sBlockIterationKey = Key.key(Namespaces.PHANTAZM, "block_iteration");
        RayTraceBlockIteration.Data sBlockIteration = new RayTraceBlockIteration.Data();
        Key sEndSelectorKey = Key.key(Namespaces.PHANTAZM, "end_selector");
        BasicShotEndpointSelector.Data sEndSelector = new BasicShotEndpointSelector.Data(sBlockIterationKey, 100);
        Key sEntityFinderKey = Key.key(Namespaces.PHANTAZM, "entity_finder");
        BetweenPointsFinder.Data sEntityFinder = new BetweenPointsFinder.Data();
        Key sTargetTesterKey = Key.key(Namespaces.PHANTAZM, "target_tester");
        PhantazmTargetTester.Data sTargetTester = new PhantazmTargetTester.Data(true);
        Key sIntersectionFinderKey = Key.key(Namespaces.PHANTAZM, "intersection_finder");
        RayTraceIntersectionFinder.Data sIntersectionFinder = new RayTraceIntersectionFinder.Data();
        Key sHeadshotTesterKey = Key.key(Namespaces.PHANTAZM, "headshot_tester");
        EyeHeightHeadshotTester.Data sHeadshotTester = new EyeHeightHeadshotTester.Data();
        Key sTargetLimiterKey = Key.key(Namespaces.PHANTAZM, "target_limiter");
        DistanceTargetLimiter.Data sTargetLimiter = new DistanceTargetLimiter.Data(1, true);
        Key sTargetFinderKey = Key.key(Namespaces.PHANTAZM, "target_finder");
        BasicTargetFinder.Data sTargetFinder = new BasicTargetFinder.Data(sEntityFinderKey, sTargetTesterKey,
                sIntersectionFinderKey, sHeadshotTesterKey, sTargetLimiterKey);
        Key sFirerKey = Key.key(Namespaces.PHANTAZM, "firer");
        HitScanFirer.Data sFirer = new HitScanFirer.Data(sEndSelectorKey, sTargetFinderKey, Collections.emptyList());
        Key gunLevelKey = Key.key(Namespaces.PHANTAZM, "gun_level");
        Key sSoundEffectAudienceProviderKey = Key.key(Namespaces.PHANTAZM, "sound_effect_audience_provider");
        EntityInstanceAudienceProvider.Data sSoundEffectAudienceProvider = new EntityInstanceAudienceProvider.Data();
        Key sSoundKey = Key.key(Namespaces.PHANTAZM, "sound_effect");
        PlaySoundEffect.Data sSound = new PlaySoundEffect.Data(sSoundEffectAudienceProviderKey,
                Sound.sound(
                        Key.key("entity.iron_golem.hurt"),
                        Sound.Source.PLAYER,
                        1.0F,
                        2.0F));
        Key sActionBarChooserKey = Key.key(Namespaces.PHANTAZM, "action_bar_chooser");
        GradientActionBarChooser.Data sActionBarChooser = new GradientActionBarChooser.Data(Component.text("RELOADING",
                null, TextDecoration.BOLD), NamedTextColor.RED, NamedTextColor.GREEN);
        Key sReloadActionBarEffectKey = Key.key(Namespaces.PHANTAZM, "reload_action_bar_effect");
        ReloadActionBarEffect.Data sReloadActionBarEffect = new ReloadActionBarEffect.Data(sStatsKey, sReloadTesterKey, sActionBarChooserKey);
        Key sClipStackMapperKey = Key.key(Namespaces.PHANTAZM, "clip_stack_mapper");
        ClipStackMapper.Data sClipStackMapper = new ClipStackMapper.Data(sReloadTesterKey);
        Map<Key, Keyed> theMap = new LinkedHashMap<>();
        theMap.put(gunLevelKey, new GunLevelData(
                0,
                ItemStack.builder(Material.WOODEN_HOE).displayName(Component.text("Tahmid's Gun")).build(),
                sStatsKey,
                sShootTesterKey,
                sReloadTesterKey,
                sFirerKey,
                List.of(sSoundKey),
                List.of(),
                List.of(sReloadActionBarEffectKey),
                Collections.emptyList(),
                List.of(sClipStackMapperKey)
        ));
        theMap.put(sStatsKey, sStats);
        theMap.put(sSoundEffectAudienceProviderKey, sSoundEffectAudienceProvider);
        theMap.put(sReloadTesterKey, sReloadTester);
        theMap.put(sShootTesterKey, sShootTester);
        theMap.put(sBlockIterationKey, sBlockIteration);
        theMap.put(sEndSelectorKey, sEndSelector);
        theMap.put(sEntityFinderKey, sEntityFinder);
        theMap.put(sTargetTesterKey, sTargetTester);
        theMap.put(sIntersectionFinderKey, sIntersectionFinder);
        theMap.put(sHeadshotTesterKey, sHeadshotTester);
        theMap.put(sTargetFinderKey, sTargetFinder);
        theMap.put(sTargetLimiterKey, sTargetLimiter);
        theMap.put(sFirerKey, sFirer);
        theMap.put(sSoundKey, sSound);
        theMap.put(sActionBarChooserKey, sActionBarChooser);
        theMap.put(sReloadActionBarEffectKey, sReloadActionBarEffect);
        theMap.put(sClipStackMapperKey, sClipStackMapper);

        ConfigCodec codec = new YamlCodec(() -> new Load(LoadSettings.builder().build()),
                () -> new Dump(DumpSettings.builder()
                        .setDefaultFlowStyle(FlowStyle.BLOCK)
                        .build()));

        try {
            ConfigBridges.write(Path.of("./gun.yaml"), codec, cfg, new ComplexData(gunLevelKey, theMap));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
