package com.github.phantazmnetwork.mob;

import com.github.phantazmnetwork.api.config.ItemStackConfigProcessors;
import com.github.phantazmnetwork.api.config.VariantConfigProcessor;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.config.MobModelConfigProcessor;
import com.github.phantazmnetwork.mob.config.goal.FollowEntityGoalConfigProcessor;
import com.github.phantazmnetwork.mob.config.goal.UseSkillGoalConfigProcessor;
import com.github.phantazmnetwork.mob.config.skill.PlaySoundSkillConfigProcessor;
import com.github.phantazmnetwork.mob.config.target.MappedSelectorConfigProcessor;
import com.github.phantazmnetwork.mob.config.target.NearestEntitiesSelectorConfigProcessor;
import com.github.phantazmnetwork.mob.goal.FollowEntityGoal;
import com.github.phantazmnetwork.mob.goal.FollowPlayerGoal;
import com.github.phantazmnetwork.mob.goal.Goal;
import com.github.phantazmnetwork.mob.goal.UseSkillGoal;
import com.github.phantazmnetwork.mob.skill.PlaySoundSkill;
import com.github.phantazmnetwork.mob.skill.Skill;
import com.github.phantazmnetwork.mob.target.FirstTargetSelector;
import com.github.phantazmnetwork.mob.target.NearestEntitiesSelector;
import com.github.phantazmnetwork.mob.target.NearestPlayersSelector;
import com.github.phantazmnetwork.mob.target.TargetSelector;
import com.github.phantazmnetwork.mob.trigger.MobTriggers;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.GroundMinestomDescriptor;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.MinestomDescriptor;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.config.GroundMinestomDescriptorConfigProcessor;
import com.github.phantazmnetwork.neuron.node.Calculator;
import com.github.phantazmnetwork.neuron.node.config.CalculatorConfigProcessor;
import com.github.steanky.ethylene.codec.yaml.YamlCodec;
import com.github.steanky.ethylene.core.bridge.ConfigBridges;
import com.github.steanky.ethylene.core.codec.ConfigCodec;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MobSerializationTest {

    @Test
    public void testSerialization() {
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
                Attribute.MAX_HEALTH.defaultValue(),
                Attribute.MOVEMENT_SPEED.defaultValue()
        );

        ConfigProcessor<Calculator> calculatorProcessor = new CalculatorConfigProcessor();
        ConfigProcessor<MinestomDescriptor> descriptorProcessor = new VariantConfigProcessor<>(Map.of(
                GroundMinestomDescriptor.SERIAL_KEY, new GroundMinestomDescriptorConfigProcessor(calculatorProcessor)
        )::get);
        ConfigProcessor<NearestEntitiesSelector<Player>> nearestPlayersSelectorProcessor = new NearestEntitiesSelectorConfigProcessor<NearestEntitiesSelector<Player>>() {
            @Override
            protected @NotNull NearestEntitiesSelector<Player> createSelector(double range, int targetLimit) {
                return new NearestPlayersSelector(range, targetLimit);
            }
        };
        ConfigProcessor<? extends TargetSelector<Player>> nearestPlayerSelector = new MappedSelectorConfigProcessor<Iterable<Player>, FirstTargetSelector<Player>>(nearestPlayersSelectorProcessor) {
            @Override
            protected @NotNull FirstTargetSelector<Player> createSelector(@NotNull TargetSelector<Iterable<Player>> delegate) {
                return new FirstTargetSelector<>(delegate);
            }
        };
        ConfigProcessor<TargetSelector<? extends Audience>> audienceSelectorProcessor = new VariantConfigProcessor<>(Map.of(
                NearestPlayersSelector.SERIAL_KEY, nearestPlayerSelector
        )::get);
        ConfigProcessor<Skill> skillProcessor = new VariantConfigProcessor<>(Map.of(
                PlaySoundSkill.SERIAL_KEY, new PlaySoundSkillConfigProcessor(audienceSelectorProcessor)
        )::get);
        ConfigProcessor<TargetSelector<Player>> playerSelectorProcessor = new VariantConfigProcessor<>(Map.of(
                NearestPlayersSelector.SERIAL_KEY, nearestPlayerSelector
        )::get);
        ConfigProcessor<FollowEntityGoal<Player>> followPlayerGoalProcessor = new FollowEntityGoalConfigProcessor<>(playerSelectorProcessor) {
            @Override
            protected @NotNull FollowEntityGoal<Player> createGoal(@NotNull TargetSelector<Player> selector) {
                return new FollowEntityGoal<>(selector) {
                    @Override
                    public @NotNull Key key() {
                        return FollowPlayerGoal.SERIAL_KEY;
                    }
                };
            }
        };
        ConfigProcessor<Goal> goalProcessor = new VariantConfigProcessor<>(Map.of(
                UseSkillGoal.SERIAL_KEY, new UseSkillGoalConfigProcessor(skillProcessor),
                FollowPlayerGoal.SERIAL_KEY, followPlayerGoalProcessor
        )::get);
        ConfigProcessor<MobModel> modelProcessor = new MobModelConfigProcessor(
                descriptorProcessor,
                goalProcessor,
                skillProcessor,
                ItemStackConfigProcessors.snbt()
        );

        try {
            ConfigCodec codec = new YamlCodec();
            String serialized = ConfigBridges.write(codec, modelProcessor, model);
            MobModel newModel = ConfigBridges.read(serialized, codec, modelProcessor);
            String reserialized = ConfigBridges.write(codec, modelProcessor, newModel);
            Assertions.assertEquals(serialized, reserialized); // TODO: actual equals check?
        } catch (IOException e) {
            Assertions.fail(e);
        }
    }

}
