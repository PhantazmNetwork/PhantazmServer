package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.api.chat.ChatChannelSendEvent;
import com.github.phantazmnetwork.api.config.ItemStackConfigProcessor;
import com.github.phantazmnetwork.api.config.KeyConfigProcessor;
import com.github.phantazmnetwork.api.config.SoundConfigProcessor;
import com.github.phantazmnetwork.api.config.VariantConfigProcessor;
import com.github.phantazmnetwork.mob.MobModel;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.mob.config.MobModelConfigProcessor;
import com.github.phantazmnetwork.mob.config.goal.FollowEntityGoalConfigProcessor;
import com.github.phantazmnetwork.mob.config.goal.UseSkillGoalConfigProcessor;
import com.github.phantazmnetwork.mob.config.skill.PlaySoundSkillConfigProcessor;
import com.github.phantazmnetwork.mob.config.target.NearestEntitySelectorConfigProcessor;
import com.github.phantazmnetwork.mob.goal.FollowEntityGoal;
import com.github.phantazmnetwork.mob.goal.GoalCreator;
import com.github.phantazmnetwork.mob.goal.UseSkillGoal;
import com.github.phantazmnetwork.mob.skill.PlaySoundSkill;
import com.github.phantazmnetwork.mob.skill.Skill;
import com.github.phantazmnetwork.mob.target.NearestEntitySelector;
import com.github.phantazmnetwork.mob.target.SerializableTargetSelector;
import com.github.phantazmnetwork.mob.target.TargetSelector;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.GroundMinestomDescriptor;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.MinestomDescriptor;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.Spawner;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.config.MinestomDescriptorConfigProcessor;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

final class MobTest {

    private MobTest() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull EventNode<Event> phantazm, @NotNull Spawner spawner) {
        TargetSelector<Iterable<Player>> underlyingSelector = new NearestEntitySelector<>(100.0, 1) {
            @Override
            protected @NotNull Optional<Player> mapTarget(@NotNull Entity entity) {
                if (entity instanceof Player targetPlayer) {
                    return Optional.of(targetPlayer);
                }

                return Optional.empty();
            }

            @Override
            protected boolean isTargetValid(@NotNull PhantazmMob mob, @NotNull Entity targetEntity,
                                            @NotNull Player target) {
                return true;
            }
        };
        TargetSelector<Player> playerSelector = targetingMob -> {
            Optional<Iterable<Player>> targetOptional = underlyingSelector.selectTarget(targetingMob);
            if (targetOptional.isPresent()) {
                Iterator<Player> target = targetOptional.get().iterator();
                if (target.hasNext()) {
                    return Optional.of(target.next());
                }
            }

            return Optional.empty();
        };

        Skill skill = new PlaySoundSkill(playerSelector, Sound.sound(
                Key.key("entity.elder_guardian.curse"),
                Sound.Source.MASTER,
                1.0F,
                1.0F
        ), true);
        MobModel model = new MobModel(
                GroundMinestomDescriptor.of(EntityType.ZOMBIE, "test"),
                List.of(
                        Collections.singleton(new FollowEntityGoal<>(playerSelector)),
                        Collections.singleton(new UseSkillGoal(skill, 5000L))
                ),
                Component.text("Test Mob"),
                Map.of(
                        EquipmentSlot.CHESTPLATE, ItemStack.of(Material.LEATHER_CHESTPLATE)
                ),
                Attribute.MAX_HEALTH.defaultValue()
        );
        Map<String, ConfigProcessor<TargetSelector<? extends Audience>>> audienceProcessors = Map.of();
        ConfigProcessor<Skill> skillProcessor = new VariantConfigProcessor<>(
                Map.of(
                        PlaySoundSkill.SERIAL_NAME, new PlaySoundSkillConfigProcessor(
                                audienceProcessors,
                                new SoundConfigProcessor(new KeyConfigProcessor())
                        )
                )
        );
        ConfigProcessor<GoalCreator> goalProcessor = new VariantConfigProcessor<>(
                Map.of(
                        UseSkillGoal.SERIAL_NAME, new UseSkillGoalConfigProcessor(skillProcessor),
                        "followNearestPlayer", new FollowEntityGoalConfigProcessor<>(new ConfigProcessor<TargetSelector<Player>>() {

                            private final ConfigProcessor<? extends TargetSelector<Iterable<Player>>> innerProcessor = new NearestEntitySelectorConfigProcessor<NearestEntitySelector<Player>>() {
                                @Override
                                protected @NotNull NearestEntitySelector<Player> createSelector(double range, int targetLimit) {
                                    return new NearestEntitySelector<>(range, targetLimit) {
                                        @Override
                                        protected @NotNull Optional<Player> mapTarget(@NotNull Entity entity) {
                                            if (entity instanceof Player player) {
                                                return Optional.of(player);
                                            }

                                            return Optional.empty();
                                        }

                                        @Override
                                        protected boolean isTargetValid(@NotNull PhantazmMob mob, @NotNull Entity targetEntity, @NotNull Player target) {
                                            return false;
                                        }
                                    };
                                }
                            };

                            @Override
                            public TargetSelector<Player> dataFromElement(@NotNull ConfigElement element) {
                                return new SerializableTargetSelector<>() {
                                    @Override
                                    public @NotNull String getSerialName() {
                                        return "followNearestPlayer";
                                    }

                                    @Override
                                    public @NotNull Optional<Player> selectTarget(@NotNull PhantazmMob mob) throws ConfigProcessException {
                                        return innerProcessor.dataFromElement(element).selectTarget(mob).map(target -> {
                                            Iterator<Player> iterator = target.iterator();
                                            if (iterator.hasNext()) {
                                                return iterator.next();
                                            }

                                            return null;
                                        });
                                    }
                                };
                            }

                            @SuppressWarnings("unchecked")
                            @Override
                            public @NotNull ConfigElement elementFromData(@NotNull TargetSelector<Player> playerTargetSelector) throws ConfigProcessException {
                                ConfigProcessor<TargetSelector<Player>> genericProcessor = (ConfigProcessor<TargetSelector<Player>>) innerProcessor;
                                return genericProcessor.elementFromData(playerTargetSelector);
                            }
                        })
                )
        );
        ConfigProcessor<MobModel> modelProcessor = new MobModelConfigProcessor(
                new MinestomDescriptorConfigProcessor(),
                goalProcessor,
                new ItemStackConfigProcessor(),
                MiniMessage.miniMessage()
        );

        AtomicReference<PhantazmMob> mobReference = new AtomicReference<>();
        phantazm.addListener(ChatChannelSendEvent.class, event -> {
            String msg = event.getInput();
            Player player = event.getPlayer();
            Instance instance = player.getInstance();

            if (instance != null) {
                switch (msg) {
                    case "spawnmob":
                        PhantazmMob mob = model.spawn(spawner, instance, player.getPosition());
                        mobReference.set(mob);
                }
            }
        });
    }

}
