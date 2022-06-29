package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.api.chat.ChatChannelSendEvent;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.Wrapper;
import com.github.phantazmnetwork.mob.MobModel;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.mob.goal.FollowPlayerGoal;
import com.github.phantazmnetwork.mob.goal.UseSkillGoal;
import com.github.phantazmnetwork.mob.skill.PlaySoundSkill;
import com.github.phantazmnetwork.mob.target.FirstTargetSelector;
import com.github.phantazmnetwork.mob.target.NearestPlayersSelector;
import com.github.phantazmnetwork.mob.target.TargetSelector;
import com.github.phantazmnetwork.mob.trigger.MobTriggers;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.GroundMinestomDescriptor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

final class MobTest {

    private MobTest() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull EventNode<Event> phantazm) {
        TargetSelector<Player> playerSelector = new FirstTargetSelector<>(new NearestPlayersSelector(20.0F, 1));
        MobModel model = new MobModel(
                Key.key(Namespaces.PHANTAZM, "mob.test"),
                GroundMinestomDescriptor.of(EntityType.ZOMBIE, "test"),
                List.of(
                        Collections.singleton(new FollowPlayerGoal(playerSelector)),
                        Collections.singleton(new UseSkillGoal(new PlaySoundSkill(playerSelector, Sound.sound(
                                Key.key("entity.elder_guardian.curse"),
                                Sound.Source.MASTER,
                                1.0F,
                                1.0F
                        ), true), 5000L))
                ),
                Map.of(
                        MobTriggers.DAMAGE_TRIGGER.key(),
                        Collections.singleton(new PlaySoundSkill(playerSelector, Sound.sound(
                                Key.key("entity.arrow.hit_player"),
                                Sound.Source.MASTER,
                                1.0F,
                                1.0F
                        ), true))
                ),
                Component.text("Test Mob"),
                Map.of(
                        EquipmentSlot.CHESTPLATE, ItemStack.of(Material.LEATHER_CHESTPLATE)
                ),
                Attribute.MAX_HEALTH.defaultValue()
        );

        Wrapper<PhantazmMob> mobReference = Wrapper.ofNull();
        phantazm.addListener(ChatChannelSendEvent.class, event -> {
            String msg = event.getInput();
            Player player = event.getPlayer();
            Instance instance = player.getInstance();

            if (instance != null) {
                switch (msg) {
                    case "spawnmob" -> {
                        PhantazmMob mob = Mob.getMobSpawner().spawn(instance, player.getPosition(), model);
                        mobReference.set(mob);
                    }
                    case "26th letter" -> {
                        mobReference.get().entity().damage(DamageType.GRAVITY, 1.0F);
                    }
                }
            }
        });
    }

}
