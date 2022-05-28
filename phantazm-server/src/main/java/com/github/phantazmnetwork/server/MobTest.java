package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.api.VecUtils;
import com.github.phantazmnetwork.api.chat.ChatChannelSendEvent;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.mob.BasicMobModel;
import com.github.phantazmnetwork.mob.MobModel;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.mob.descriptor.GroundPhantazmMobDescriptor;
import com.github.phantazmnetwork.mob.skill.TestSkill;
import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.MinestomDescriptor;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntity;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

final class MobTest {

    private MobTest() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull EventNode<Event> phantazm) {
        AtomicReference<PhantazmMob<NeuralEntity>> mobRef = new AtomicReference<>();
        AtomicReference<Vec3I> destination = new AtomicReference<>();
        MinestomDescriptor descriptor = new GroundPhantazmMobDescriptor(EntityType.PIG, "test") {
            @Override
            protected @Nullable Vec3I chooseDestination(@NotNull Agent agent) {
                return destination.get();
            }
        };

        String testSkillName = "testSkill";
        MobModel<MinestomDescriptor, NeuralEntity> model = new BasicMobModel<>(
                descriptor,
                Map.of(
                        testSkillName, new TestSkill()
                ),
                Component.text("Hi!"),
                Map.of(
                        EquipmentSlot.CHESTPLATE, ItemStack.of(Material.LEATHER_CHESTPLATE)
                )
        );

        phantazm.addListener(ChatChannelSendEvent.class, event -> {
            String msg = event.getInput();
            Player player = event.getPlayer();
            Instance instance = player.getInstance();

            if (instance != null) {
                switch (msg) {
                    case "makenew" -> mobRef.set(model.spawn(Neuron.getSpawner(), instance, player.getPosition()));
                    case "goto" -> destination.set(VecUtils.toBlockInt(player.getPosition()));
                    case "testskill" -> {
                        PhantazmMob<?> mob = mobRef.get();
                        mob.model().getSkills().get(testSkillName).use(mob);
                    }
                }
            }
        });
    }

}
