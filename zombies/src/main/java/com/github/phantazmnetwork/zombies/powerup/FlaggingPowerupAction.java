package com.github.phantazmnetwork.zombies.powerup;

import com.github.phantazmnetwork.zombies.map.Flaggable;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

@Model("zombies.powerup.action.flagging")
@Cache(false)
public class FlaggingPowerupAction extends PowerupActionBase {
    private final Data data;
    private final Flaggable flaggable;

    @FactoryMethod
    public FlaggingPowerupAction(@NotNull Data data,
            @NotNull @DataName("deactivation_predicate") DeactivationPredicate deactivationPredicate,
            @NotNull @Dependency("zombies.dependency.map_object.flaggable") Flaggable flaggable) {
        super(deactivationPredicate);
        this.data = data;
        this.flaggable = flaggable;
    }

    @Override
    public void activate(@NotNull ZombiesPlayer player, long time) {
        super.activate(player, time);
        flaggable.setFlag(data.flag);
    }

    @Override
    public void deactivate() {
        flaggable.clearFlag(data.flag);
    }

    @DataObject
    public record Data(@NotNull Key flag, @NotNull @DataPath("deactivation_predicate") String deactivationPredicate) {
    }
}
