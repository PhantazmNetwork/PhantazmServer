package com.github.phantazmnetwork.zombies.powerup;

import com.github.phantazmnetwork.zombies.map.Flaggable;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

@Model("zombies.powerup.action.map_flagging")
public class MapFlaggingPowerupActionBuilder implements Supplier<PowerupAction> {
    private final Data data;
    private final Supplier<DeactivationPredicate> deactivationPredicate;
    private final Flaggable flaggable;

    @FactoryMethod
    public MapFlaggingPowerupActionBuilder(@NotNull Data data,
            @NotNull @DataName("deactivation_predicate") Supplier<DeactivationPredicate> deactivationPredicate,
            @NotNull @Dependency("zombies.dependency.map_object.flaggable") Flaggable flaggable) {
        this.data = Objects.requireNonNull(data, "data");
        this.deactivationPredicate = Objects.requireNonNull(deactivationPredicate, "deactivationPredicate");
        this.flaggable = Objects.requireNonNull(flaggable, "flaggable");
    }

    @Override
    public PowerupAction get() {
        return new MapFlaggingPowerupAction(data, deactivationPredicate.get(), flaggable);
    }

    @DataObject
    public record Data(@NotNull Key flag, @NotNull @DataPath("deactivation_predicate") String deactivationPredicate) {
    }

    private static class MapFlaggingPowerupAction extends PowerupActionBase {
        private final Data data;
        private final Flaggable flaggable;

        private MapFlaggingPowerupAction(Data data, DeactivationPredicate deactivationPredicate, Flaggable flaggable) {
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
        public void deactivate(@NotNull ZombiesPlayer player) {
            flaggable.clearFlag(data.flag);
        }
    }
}
