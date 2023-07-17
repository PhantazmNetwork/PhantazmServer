package org.phantazm.mob.target;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.validator.TargetValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Model("mob.selector.all_players")
@Cache(false)
public class AllPlayersSelector implements TargetSelector<List<Player>> {
    private final TargetValidator targetValidator;

    @FactoryMethod
    public AllPlayersSelector(@NotNull @Child("validator") TargetValidator targetValidator) {
        this.targetValidator = targetValidator;
    }

    @Override
    public @NotNull Optional<List<Player>> selectTarget(@NotNull PhantazmMob self) {
        Instance instance = self.entity().getInstance();
        if (instance != null) {
            Set<Player> players = instance.getPlayers();
            if (players.size() > 0) {
                List<Player> playerList = new ArrayList<>(players.size());
                for (Player player : players) {
                    if (targetValidator.valid(self.entity(), player)) {
                        playerList.add(player);
                    }
                }

                if (playerList.size() > 0) {
                    return Optional.of(List.copyOf(playerList));
                }
            }
        }

        return Optional.empty();
    }

    @DataObject
    public record Data(@NotNull @ChildPath("validator") String targetValidator) {
    }
}
