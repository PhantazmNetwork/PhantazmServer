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
import com.github.phantazmnetwork.zombies.equipment.gun.effect.*;
import com.github.phantazmnetwork.zombies.equipment.gun.shot.BasicShotEndSelector;
import com.github.phantazmnetwork.zombies.equipment.gun.shot.handler.*;
import com.github.phantazmnetwork.zombies.equipment.gun.target.LinearTargetSelector;
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
            for (Player player : event.getInstance().getPlayers()) {
                InventoryProfile profile = profileSwitchers.get(player.getUuid()).getCurrentProfile();
                for (int slot = 0; slot < 9; slot++) {
                    if (profile.hasInventoryObject(slot)) {
                        InventoryObject object = profile.getInventoryObject(slot);
                        if (object instanceof Equipment equipment) {
                            equipment.leftClick();
                        }
                    }
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
                    List<GunLevel> levels = Collections.singletonList(
                            new GunLevel(
                                    ItemStack.builder(Material.WOODEN_HOE)
                                            .displayName(Component.text("Pistol", NamedTextColor.GOLD))
                                            .build(),
                                    new LinearTargetSelector(new BasicShotEndSelector(120)),
                                    List.of(
                                            new PlaySoundEffect(Sound.sound(
                                                    Key.key("entity.horse.gallop"),
                                                    Sound.Source.PLAYER,
                                                    1.0F,
                                                    0.5F
                                            ))
                                    ),
                                    List.of(
                                            new GradientReloadActionBarEffect(Component.text("RELOADING",
                                                    null, TextDecoration.BOLD), NamedTextColor.RED,
                                                    NamedTextColor.GREEN),
                                            new AmmoLevelEffect(),
                                            new ShootExpEffect()
                                    ),
                                    Collections.singletonList(new SendMessageEffect(Component.text("You're out of ammo.", NamedTextColor.RED))),
                                    List.of(
                                            new GunEffectShotHandler(new PlaySoundEffect(Sound.sound(
                                                    Key.key("entity.iron_golem.hurt"),
                                                    Sound.Source.PLAYER,
                                                    1.0F,
                                                    2.0F
                                            ))),
                                            new GuardianBeamShotHandler(
                                                    false,
                                                    50L
                                            )/*new ParticleTrailEffect(
                                                    Particle.CRIT,
                                                    false,
                                                    0.0F,
                                                    0.0F,
                                                    0.0F,
                                                    0.0F,
                                                    1,
                                                    4
                                            )*/,
                                            new DamageShotHandler(3.0F, 5.0F),
                                            new KnockbackShotHandler(5.0D, 5.0D),
                                            new FeedbackShotHandler(
                                                    Component.text("Regular Hit", NamedTextColor.GOLD),
                                                    Component.text("Critical Hit", NamedTextColor.GOLD,
                                                            TextDecoration.BOLD)
                                            ),
                                            new SoundShotHandler(
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
                                            )
                                    ),
                                    List.of(
                                            new ClipStackMapper(),
                                            new ReloadStackMapper()
                                    ),
                                    10L,
                                    30L,
                                    36,
                                    12,
                                    3
                            )
                    );
                    GunModel model = new GunModel() {
                        @Override
                        public @NotNull List<GunLevel> getLevels() {
                            return levels;
                        }

                        @Override
                        public @NotNull Key key() {
                            return Key.key(Namespaces.PHANTAZM, "gun.test");
                        }
                    };
                    PlayerView view = viewProvider.fromPlayer(event.getPlayer());
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
