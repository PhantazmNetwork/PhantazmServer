package org.phantazm.server;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.client.play.ClientSteerVehiclePacket;
import org.jetbrains.annotations.NotNull;
import org.phantazm.neuron.bindings.minestom.entity.GroundMinestomDescriptor;
import org.phantazm.neuron.bindings.minestom.entity.Spawner;

import java.util.Set;

final class NeuronTest {
    private NeuronTest() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull EventNode<Event> global, @NotNull Spawner spawner) {
        GroundMinestomDescriptor testDescriptor = GroundMinestomDescriptor.of(EntityType.PHANTOM, "phantom");

        global.addListener(EventListener.builder(PlayerChatEvent.class).ignoreCancelled(false).handler(event -> {
            String msg = event.getMessage();
            Player player = event.getPlayer();
            Instance instance = player.getInstance();

            if (instance != null) {
                switch (msg) {
                    case "T" -> spawner.spawnEntity(instance, player.getPosition().add(5, 0, 0), testDescriptor,
                            neuralEntity -> {
                                //neuralEntity.setGravity(0, 0.2);
                                neuralEntity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.1F);
                            }).setTarget(player);
                    case "TT" -> {
                        for (int i = 0; i < 500; i++) {
                            spawner.spawnEntity(instance, player.getPosition(), testDescriptor).setTarget(player);
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

        global.addListener(PlayerEntityInteractEvent.class, event -> {
            Entity target = event.getTarget();
            Entity player = event.getPlayer();

            if (player.getPassengers().contains(target)) {
                return;
            }

            if (player.getVehicle() != null) {
                player.getVehicle().removePassenger(player);
            }

            event.getTarget().addPassenger(event.getPlayer());
        });

        global.addListener(PlayerPacketEvent.class, event -> {
            if (event.getPlayer().getVehicle() == null) {
                return;
            }

            if (event.getPacket() instanceof ClientSteerVehiclePacket packet) {
                if (packet.flags() == 2) {
                    event.getPlayer().getVehicle().removePassenger(event.getPlayer());
                }
            }
        });
    }
}
