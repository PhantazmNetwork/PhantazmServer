package org.phantazm.zombies.powerup.action;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Flaggable;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicate;
import org.phantazm.zombies.powerup.Powerup;

import java.util.Objects;
import java.util.function.Supplier;

@Model("zombies.powerup.action.map_flagging")
public class MapFlaggingAction implements Supplier<PowerupAction> {
    private final Data data;
    private final Supplier<DeactivationPredicate> deactivationPredicate;
    private final Flaggable flaggable;

    @FactoryMethod
    public MapFlaggingAction(@NotNull Data data,
            @NotNull @Child("deactivation_predicate") Supplier<DeactivationPredicate> deactivationPredicate,
            @NotNull Flaggable flaggable) {
        this.data = Objects.requireNonNull(data, "data");
        this.deactivationPredicate = Objects.requireNonNull(deactivationPredicate, "deactivationPredicate");
        this.flaggable = Objects.requireNonNull(flaggable, "flaggable");
    }

    @Override
    public PowerupAction get() {
        return new Action(data, deactivationPredicate.get(), flaggable);
    }

    @DataObject
    public record Data(@NotNull Key flag, @NotNull @ChildPath("deactivation_predicate") String deactivationPredicate) {
    }

    private static class Action extends PowerupActionBase {
        private final Data data;
        private final Flaggable flaggable;

        private Action(Data data, DeactivationPredicate deactivationPredicate, Flaggable flaggable) {
            super(deactivationPredicate);
            this.data = data;
            this.flaggable = flaggable;
        }

        @Override
        public void activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer player, long time) {
            super.activate(powerup, player, time);
            flaggable.setFlag(data.flag);
        }

        @Override
        public void deactivate(@NotNull ZombiesPlayer player) {
            flaggable.clearFlag(data.flag);
        }
    }
}
