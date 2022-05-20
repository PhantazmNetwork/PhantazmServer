package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.api.chat.ChatChannelSendEvent;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.GroundMinestomDescriptor;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntity;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.Spawner;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

final class NeuronTest {
    private NeuronTest() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull EventNode<Event> global, @NotNull Spawner spawner,
                           @NotNull EventNode<Event> phantazm) {
        GroundMinestomDescriptor testDescriptor = GroundMinestomDescriptor.of(EntityType.PHANTOM, "phantom");

        phantazm.addListener(ChatChannelSendEvent.class, event -> {
            String msg = event.getInput();
            Player player = event.getPlayer();
            Instance instance = player.getInstance();

            if(instance != null) {
                switch (msg) {
                    case "T" -> spawner.spawnEntity(instance, player.getPosition().add(5, 0, 0), testDescriptor,
                            NeuralEntity::new, neuralEntity -> {
                        //neuralEntity.setGravity(0, 0.2);
                        neuralEntity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.1F);
                            }).setTarget(player);
                    case "TT" -> {
                        for(int i = 0; i < 500; i++) {
                            spawner.spawnEntity(instance, player.getPosition(), testDescriptor, NeuralEntity::new)
                                    .setTarget(player);
                        }
                    }
                    case "Z" -> {
                        Pos playerPos = player.getPosition();
                        instance.setBlock(playerPos.blockX(), playerPos.blockY(), playerPos.blockZ(), Block.GOLD_BLOCK);
                    }
                    case "C" -> event.getPlayer().setGameMode(GameMode.CREATIVE);
                    case "ZZ" -> {
                        EntityCreature creature = new EntityCreature(EntityType.ZOMBIE);
                        creature.setInstance(instance, player.getPosition().add(5, 0, 0));

                        global.addListener(PlayerMoveEvent.class, moveEvent -> creature.getNavigator().setPathTo(
                                moveEvent.getPlayer().getPosition()));
                    }
                }
            }
        });

        global.addListener(PlayerSpawnEvent.class, event -> {
            if(!event.isFirstSpawn()) {
                return;
            }

            event.getPlayer().teleport(new Pos(0, 100, 0));
            event.getPlayer().setGameMode(GameMode.CREATIVE);
            event.getPlayer().setFlying(true);
            Pos start = event.getPlayer().getPosition().sub(0, 1, 0);

            for(int i = 0; i < 100; i++) {
                for(int j = 0; j < 100; j++) {
                    event.getSpawnInstance().setBlock(start.add(i, 0, j), Block.GOLD_BLOCK);
                }
            }
        });
    }
}
