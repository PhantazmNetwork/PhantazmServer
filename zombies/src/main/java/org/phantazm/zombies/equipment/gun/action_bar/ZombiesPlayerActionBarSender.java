package org.phantazm.zombies.equipment.gun.action_bar;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.action_bar.ZombiesPlayerActionBar;

import java.util.Objects;

@Model("zombies.gun.action_bar.zombies_player_sender")
@Cache(false)
public class ZombiesPlayerActionBarSender implements ActionBarSender {

    private final Data data;

    private final ZombiesPlayerActionBar actionBar;

    @FactoryMethod
    public ZombiesPlayerActionBarSender(@NotNull Data data, @NotNull ZombiesPlayerActionBar actionBar) {
        this.data = Objects.requireNonNull(data, "data");
        this.actionBar = Objects.requireNonNull(actionBar, "actionBar");
    }

    @Override
    public void sendActionBar(@NotNull Component message) {
        actionBar.sendActionBar(message, data.priority());
    }

    @DataObject
    public record Data(int priority) {

    }

}
