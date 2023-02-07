package org.phantazm.mob.target;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.validator.AlwaysValid;
import org.phantazm.mob.validator.TargetValidator;

import java.util.Optional;

/**
 * A {@link TargetSelector} that selects nearby players.
 */
@Model("mob.selector.nearest_players")
@Cache(false)
public class NearestPlayersSelector extends NearestEntitiesSelector<Player> {

    /**
     * Creates a {@link NearestPlayersSelector}.
     */
    @FactoryMethod
    public NearestPlayersSelector(@NotNull Data data, @NotNull @Child("validator") TargetValidator targetValidator) {
        super(data.range(), data.targetLimit(), targetValidator);
    }

    @Override
    protected @NotNull Optional<Player> mapTarget(@NotNull Entity entity) {
        if (entity instanceof Player player) {
            return Optional.of(player);
        }

        return Optional.empty();
    }

    @DataObject
    public record Data(double range, int targetLimit, @NotNull @ChildPath("validator") String targetValidatorPath) {

    }

}
