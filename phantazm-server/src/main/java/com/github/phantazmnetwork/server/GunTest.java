package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.api.chat.ChatChannelSendEvent;
import com.github.phantazmnetwork.api.inventory.*;
import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.api.player.PlayerViewProvider;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.MobModel;
import com.github.phantazmnetwork.mob.goal.FollowPlayerGoal;
import com.github.phantazmnetwork.mob.target.FirstTargetSelector;
import com.github.phantazmnetwork.mob.target.NearestPlayersSelector;
import com.github.phantazmnetwork.mob.target.TargetSelector;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.GroundMinestomDescriptor;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.equipment.gun.Gun;
import com.github.phantazmnetwork.zombies.equipment.gun.GunLevel;
import com.github.phantazmnetwork.zombies.equipment.gun.GunModel;
import com.github.phantazmnetwork.zombies.equipment.gun.GunStats;
import com.github.phantazmnetwork.zombies.equipment.gun.effect.*;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.ReloadTester;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.StateReloadTester;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.actionbar.GradientActionBarChooser;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.actionbar.ReloadActionBarChooser;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.ShootTester;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.StateShootTester;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint.BasicShotEndpointSelector;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint.ShotEndpointSelector;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.Firer;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.HitScanFirer;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.ProjectileFirer;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.SpreadFirer;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler.*;
import com.github.phantazmnetwork.zombies.equipment.gun.target.BasicTargetFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.TargetFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.directional.AroundEndFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.directional.BetweenPointsFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.directional.DirectionalEntityFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.positional.NearbyPhantazmMobFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.positional.PositionalEntityFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.headshot.EyeHeightHeadshotTester;
import com.github.phantazmnetwork.zombies.equipment.gun.target.headshot.HeadshotTester;
import com.github.phantazmnetwork.zombies.equipment.gun.target.headshot.StaticHeadshotTester;
import com.github.phantazmnetwork.zombies.equipment.gun.target.tester.RayTraceTargetTester;
import com.github.phantazmnetwork.zombies.equipment.gun.target.tester.StaticTargetTester;
import com.github.phantazmnetwork.zombies.equipment.gun.target.tester.TargetTester;
import com.github.phantazmnetwork.zombies.equipment.gun.visual.ClipStackMapper;
import com.github.phantazmnetwork.zombies.equipment.gun.visual.ReloadStackMapper;
import net.kyori.adventure.key.Key;
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
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.PlayerHeadMeta;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;

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

                    Key endpointSelectorKey = Key.key(Namespaces.PHANTAZM, "gun.endpoint_selector.test");
                    ShotEndpointSelector endpointSelector = new BasicShotEndpointSelector(new BasicShotEndpointSelector.Data(100), view);
                    Key entityFinderKey = Key.key(Namespaces.PHANTAZM, "gun.entity_finder.test");
                    DirectionalEntityFinder directionalEntityFinder = new BetweenPointsFinder(new BetweenPointsFinder.Data());
                    Key headshotTesterKey = Key.key(Namespaces.PHANTAZM, "gun.headshot_tester.test");
                    HeadshotTester headshotTester = new StaticHeadshotTester(new StaticHeadshotTester.Data(false));
                    Key targetTesterKey = Key.key(Namespaces.PHANTAZM, "gun.target_tester.test");
                    TargetTester targetTester = new RayTraceTargetTester(new RayTraceTargetTester.Data());
                    Key targetFinderKey = Key.key(Namespaces.PHANTAZM, "gun.target_finder.test");
                    TargetFinder targetFinder = new BasicTargetFinder(new BasicTargetFinder.Data(entityFinderKey,
                            targetTesterKey, headshotTesterKey, false, 2),
                            Mob.getMobStore(), directionalEntityFinder, targetTester, headshotTester);
                    Key statsKey = Key.key(Namespaces.PHANTAZM, "gun.stats.test");
                    GunStats stats = new GunStats(10L, 30L, 36, 12, 1);
                    Key actionBarChooserKey = Key.key(Namespaces.PHANTAZM, "gun.action_bar.chooser.test");
                    ReloadActionBarChooser actionBarChooser = new GradientActionBarChooser(new GradientActionBarChooser.Data(Component.text("RELOADING",
                            null, TextDecoration.BOLD), NamedTextColor.RED, NamedTextColor.GREEN));
                    Key reloadTesterKey = Key.key(Namespaces.PHANTAZM, "gun.reload_tester.test");
                    ReloadTester reloadTester = new StateReloadTester(new StateReloadTester.Data(statsKey), stats);
                    Key shootTesterKey = Key.key(Namespaces.PHANTAZM, "gun.shoot_tester.test");
                    ShootTester shootTester = new StateShootTester(new StateShootTester.Data(statsKey, reloadTesterKey), stats, reloadTester);

                    Key damageShotHandlerKey = Key.key(Namespaces.PHANTAZM, "gun.shot_handler.damage.test");
                    ShotHandler damage = new DamageShotHandler(new DamageShotHandler.Data(3.0F, 5.0F));

                    Collection<Key> shotHandlerKeys = List.of(
                            Key.key(Namespaces.PHANTAZM, "gun.effect.play_sound.test"),
                            Key.key(Namespaces.PHANTAZM, "gun.shot_handler.particle.test"),
                            damageShotHandlerKey,
                            Key.key(Namespaces.PHANTAZM, "gun.shot_handler.feedback.test"),
                            Key.key(Namespaces.PHANTAZM, "gun.shot_handler.sound.test"),
                            Key.key(Namespaces.PHANTAZM, "gun.shot_handler.chain.test")
                    );
                    Collection<ShotHandler> shotHandlers = List.of(
                            new GunEffectShotHandler(new PlaySoundEffect(new PlaySoundEffect.Data(Sound.sound(Key.key("entity.iron_golem.hurt"),
                                    Sound.Source.PLAYER,
                                    1.0F,
                                    2.0F
                            )), view)),
                            damage,
                            new KnockbackShotHandler(new KnockbackShotHandler.Data(5.0D, 5.0D)),
                            new FeedbackShotHandler(new FeedbackShotHandler.Data(
                                    Component.text("Regular Hit", NamedTextColor.GOLD),
                                    Component.text("Critical Hit", NamedTextColor.GOLD,
                                            TextDecoration.BOLD)
                            )),
                            new SoundShotHandler(new SoundShotHandler.Data(
                                    Sound.sound(
                                            Key.key("entity.arrow.hit_player"),
                                            Sound.Source.PLAYER,
                                            1.0F,
                                            2.0F
                                    ),
                                    Sound.sound(
                                            Key.key("entity.arrow.hit_player"),
                                            Sound.Source.PLAYER,
                                            1.0F,
                                            1.5F
                                    )
                            )),
                            new ParticleTrailShotHandler(new ParticleTrailShotHandler.Data(
                                    Particle.CRIT,
                                    false,
                                    0.0F,
                                    0.0F,
                                    0.0F,
                                    0.0F,
                                    1,
                                    4
                            ))
                    );

                    ProjectileFirer projectileFirer = new ProjectileFirer(new ProjectileFirer.Data(endpointSelectorKey,
                            targetFinderKey, shotHandlerKeys, EntityType.FIREBALL, 1.0, 0, 60L),
                            view, endpointSelector, targetFinder, shotHandlers);
                    global.addListener(ProjectileCollideWithBlockEvent.class, projectileFirer::onProjectileCollision);
                    global.addListener(ProjectileCollideWithEntityEvent.class, projectileFirer::onProjectileCollision);

                    // swap out as necessary
                    TargetTester staticTargetTester = new StaticTargetTester(new StaticTargetTester.Data());
                    HeadshotTester eyeHeight = new EyeHeightHeadshotTester(new EyeHeightHeadshotTester.Data());
                    DirectionalEntityFinder aroundEnd = new AroundEndFinder(new AroundEndFinder.Data(3F));
                    Key hitScanKey = Key.key(Namespaces.PHANTAZM, "gun.firer.hit_scan.test");
                    Firer hitScan = new HitScanFirer(new HitScanFirer.Data(endpointSelectorKey, targetFinderKey,
                            shotHandlerKeys), view, endpointSelector, targetFinder, shotHandlers);
                    ShotHandler explosion = new ExplosionShotHandler(new ExplosionShotHandler.Data(3F));
                    Key subFinderKey = Key.key(Namespaces.PHANTAZM, "gun.entity_finder.positional.test");
                    PositionalEntityFinder subFinder = new NearbyPhantazmMobFinder(new NearbyPhantazmMobFinder.Data(3),
                            Mob.getMobStore());
                    Key subFirerKey = Key.key(Namespaces.PHANTAZM, "gun.firer.hit_scan.test");
                    Firer subFirer = new HitScanFirer(new HitScanFirer.Data(endpointSelectorKey, targetFinderKey,
                            Collections.singleton(damageShotHandlerKey)), view, endpointSelector, targetFinder,
                            Collections.singleton(damage));
                    ShotHandler chain = new ChainShotHandler(new ChainShotHandler.Data(subFinderKey, subFirerKey,
                            true, 3),
                            subFinder, subFirer);

                    // don't use nCopies bc of repetition, but for the sake of being concise
                    Firer spread = new SpreadFirer(new SpreadFirer.Data(Collections.nCopies(4, hitScanKey),
                            (float) Math.toRadians(15.0)), new Random(), Collections.nCopies(4, hitScan));

                    List<GunLevel> levels = Collections.singletonList(
                            new GunLevel(
                                    ItemStack.builder(Material.WOODEN_HOE)
                                            .displayName(Component.text("Pistol", NamedTextColor.GOLD))
                                            .build(),
                                    stats,
                                    shootTester,
                                    reloadTester,
                                    spread,
                                    List.of(
                                            new PlaySoundEffect(new PlaySoundEffect.Data(Sound.sound(
                                                    Key.key("entity.horse.gallop"),
                                                    Sound.Source.PLAYER,
                                                    1.0F,
                                                    0.5F
                                            )), view)
                                    ),
                                    List.of(
                                            new ReloadActionBarEffect(new ReloadActionBarEffect.Data(statsKey,
                                                    reloadTesterKey, actionBarChooserKey), view, stats, reloadTester,
                                                    actionBarChooser),
                                            new AmmoLevelEffect(new AmmoLevelEffect.Data(), view),
                                            new ShootExpEffect(new ShootExpEffect.Data(statsKey), view, stats)
                                    ),
                                    Collections.singletonList(new SendMessageEffect(new SendMessageEffect.Data(Component.text("You're out of ammo.",
                                            NamedTextColor.RED)), view)),
                                    List.of(
                                            new ClipStackMapper(new ClipStackMapper.Data(reloadTesterKey), reloadTester),
                                            new ReloadStackMapper(new ReloadStackMapper.Data(statsKey, reloadTesterKey),
                                                    stats, reloadTester)
                                    )
                            )
                    );
                    GunModel model = new GunModel(levels, Key.key(Namespaces.PHANTAZM, "gun.test"));
                    Gun gun = new Gun(view, model, Mob.getMobStore());

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
                            Attribute.MAX_HEALTH.defaultValue(),
                            Attribute.MOVEMENT_SPEED.defaultValue() / 3.0F
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
    }

}
