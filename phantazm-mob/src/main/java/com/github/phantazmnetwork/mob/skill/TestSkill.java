package com.github.phantazmnetwork.mob.skill;

import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.LinearComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public class TestSkill implements Skill {
    @Override
    public void use(@NotNull PhantazmMob<?> sender) {
        Instance instance = sender.entity().getInstance();
        if (instance == null) {
            throw new IllegalStateException();
        }

        Component name = sender.entity().getCustomName();
        if (name == null) {
            name = Component.text(sender.entity().getEntityType().name());
        }

        Pos pos = sender.entity().getPosition();
        Component posComponent = Component.join(JoinConfiguration.separator(Component.text(", ")), Component.text(pos.x()), Component.text(pos.y()), Component.text(pos.z()));
        instance.sendMessage(LinearComponents.linear(name, Component.text(" is at ("), posComponent, Component.text(").")));
    }
}
