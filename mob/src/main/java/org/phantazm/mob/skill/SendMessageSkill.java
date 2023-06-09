package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.target.TargetSelector;

import java.util.List;
import java.util.Optional;

@Model("mob.skill.send_message")
@Cache(false)
public class SendMessageSkill implements Skill {
    private final Data data;
    private final TargetSelector<List<Player>> selector;

    @FactoryMethod
    public SendMessageSkill(@NotNull Data data, @NotNull @Child("selector") TargetSelector<List<Player>> selector) {
        this.data = data;
        this.selector = selector;
    }

    @Override
    public void use(@NotNull PhantazmMob self) {
        Optional<List<Player>> playerListOptional = selector.selectTarget(self);
        if (playerListOptional.isPresent()) {
            List<Player> targets = playerListOptional.get();
            for (Player target : targets) {
                target.sendMessage(data.message);
            }
        }
    }

    @DataObject
    public record Data(@NotNull Component message, @NotNull @ChildPath("selector") String selector) {
    }
}
