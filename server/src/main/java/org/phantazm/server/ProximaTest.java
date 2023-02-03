package org.phantazm.server;

import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ChangeGameStatePacket;
import net.minestom.server.network.packet.server.play.PlayerAbilitiesPacket;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.phantazm.proxima.bindings.minestom.GroundPathfindingFactory;
import org.phantazm.proxima.bindings.minestom.Pathfinding;
import org.phantazm.proxima.bindings.minestom.Spawner;

import java.util.Set;

final class ProximaTest {
    private ProximaTest() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull EventNode<Event> global, @NotNull Spawner spawner) {
        Pathfinding.Factory factory = new GroundPathfindingFactory(new GroundPathfindingFactory.Data(1, 4, 0.5F));

        global.addListener(EventListener.builder(PlayerChatEvent.class).ignoreCancelled(false).handler(event -> {
            String msg = event.getMessage();
            Player player = event.getPlayer();
            Instance instance = player.getInstance();

            if (instance != null) {
                switch (msg) {
                    case "T" -> spawner.spawn(instance, player.getPosition().add(5, 0, 0), EntityType.ZOMBIE, factory)
                            .setDestination(player);
                    case "TT" -> {
                        for (int i = 0; i < 500; i++) {
                            spawner.spawn(instance, player.getPosition(), EntityType.ZOMBIE, factory)
                                    .setDestination(player);
                        }
                    }
                    case "Z" -> {
                        Pos playerPos = player.getPosition();
                        instance.setBlock(playerPos.blockX(), playerPos.blockY(), playerPos.blockZ(), Block.GOLD_BLOCK);
                    }
                    case "L" -> {
                        Pos playerPos = player.getPosition();
                        instance.setBlock(playerPos.blockX(), playerPos.blockY(), playerPos.blockZ(), Block.LADDER);
                    }
                    case "C" -> event.getPlayer().setGameMode(GameMode.CREATIVE);
                    case "S" -> event.getPlayer().setGameMode(GameMode.SURVIVAL);
                    case "A" -> event.getPlayer().setGameMode(GameMode.ADVENTURE);
                    case "ZZ" -> {
                        EntityCreature creature = new EntityCreature(EntityType.ZOMBIE);
                        creature.setInstance(instance, player.getPosition().add(5, 0, 0));

                        global.addListener(PlayerMoveEvent.class,
                                moveEvent -> creature.getNavigator().setPathTo(moveEvent.getPlayer().getPosition()));
                    }
                    case "V" -> {
                        Set<Entity> entities = instance.getEntities();
                        for (Entity entity : entities) {
                            entity.setVelocity(new Vec(10, 0, 0));
                        }
                    }
                    case "W" -> {
                        Entity entity = new Entity(EntityType.ZOMBIE);
                        entity.setInstance(player.getInstance(), player.getPosition()).join();
                        entity.setInvisible(true);
                    }
                    case "succ" -> {
                        player.sendPacket(new ChangeGameStatePacket(ChangeGameStatePacket.Reason.BEGIN_RAINING, 0));
                        player.sendPacket(new ChangeGameStatePacket(ChangeGameStatePacket.Reason.RAIN_LEVEL_CHANGE,
                                Float.MIN_VALUE));
                    }
                    case "stop" -> {
                        player.sendPacket(new ChangeGameStatePacket(ChangeGameStatePacket.Reason.END_RAINING, 0));
                    }
                    case "succc" -> {
                        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
                            player.sendPacket(new ChangeGameStatePacket(
                                    ChangeGameStatePacket.Reason.PLAYER_ELDER_GUARDIAN_MOB_APPEARANCE, 5));
                        }, TaskSchedule.immediate(), TaskSchedule.millis(500));
                    }
                    case "vegetals" -> {
                        player.sendPacket(new ChangeGameStatePacket(ChangeGameStatePacket.Reason.DEMO_EVENT, 0));
                    }
                    case "noob" -> {
                        player.sendPacket(new ChangeGameStatePacket(ChangeGameStatePacket.Reason.DEMO_EVENT, 104));
                    }
                    case "fish" -> {
                        player.sendPacket(
                                new ChangeGameStatePacket(ChangeGameStatePacket.Reason.PLAY_PUFFERFISH_STING_SOUND, 0));
                    }
                    case "sonic" -> {
                        player.sendPacket(new PlayerAbilitiesPacket((byte)0x6, 50f, 50f));
                    }
                    case "zz_slow_low_iq" -> {
                        player.sendPacket(new PlayerAbilitiesPacket((byte)0x6, 0.05f, 0.05f));
                    }
                }
            }
        }).build());

        global.addListener(PlayerSpawnEvent.class, event -> {
            if (!event.isFirstSpawn()) {
                return;
            }

            event.getPlayer().teleport(new Pos(0, 100, 0)).join();
            event.getPlayer().setGameMode(GameMode.CREATIVE);
            event.getPlayer().setFlying(true);
            Pos start = event.getPlayer().getPosition().sub(0, 1, 0);

            for (int i = 0; i < 100; i++) {
                for (int j = 0; j < 100; j++) {
                    event.getSpawnInstance().setBlock(start.add(i, 0, j), Block.GOLD_BLOCK);
                }
            }
        });
    }
}
