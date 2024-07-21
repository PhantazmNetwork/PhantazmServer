package org.phantazm.zombies.powerup.predicate;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.core.AttributeUtils;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Optional;

@Model("zombies.powerup.deactivation_predicate.timed")
public class TimedDeactivationPredicate implements DeactivationPredicateComponent {
    private final Data data;

    @FactoryMethod
    public TimedDeactivationPredicate(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull DeactivationPredicate apply(@NotNull ZombiesScene scene) {
        return new Predicate(data);
    }

    @DataObject
    public record Data(long time) {

    }

    private static class Predicate implements DeactivationPredicate {
        private final Data data;
        private volatile int startTick = -1;
        private volatile int time;

        private Predicate(Data data) {
            this.data = data;
        }

        @Override
        public void activate(@NotNull Powerup powerup, @Nullable ZombiesPlayer zombiesPlayer, long time) {
            startTick = MinecraftServer.currentTick();

            Attribute attribute = Attributes.getOrRegister("phantazm.powerup.duration." + powerup.key().value(), 0);
            Optional<Player> playerOptional;
            if (zombiesPlayer == null || (playerOptional = zombiesPlayer.getPlayer()).isEmpty()) {
                this.time = (int) data.time;
            } else {
                this.time = Math.round(AttributeUtils.computeWithBase(data.time, playerOptional.get().getAttribute(attribute)));
            }
        }

        @Override
        public boolean shouldDeactivate(long time) {
            if (startTick < 0) {
                return false;
            }

            return MinecraftServer.currentTick() - startTick >= this.time;
        }
    }
}
