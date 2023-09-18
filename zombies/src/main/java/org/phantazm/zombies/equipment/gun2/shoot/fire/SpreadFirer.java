package org.phantazm.zombies.equipment.gun2.shoot.fire;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.PlayerComponent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.*;

public class SpreadFirer implements PlayerComponent<Firer> {

    private final Data data;

    private final Random random;

    private final List<PlayerComponent<Firer>> subFirers;

    public SpreadFirer(@NotNull Data data, @NotNull Random random,
        @NotNull Collection<PlayerComponent<Firer>> subFirers) {
        this.data = Objects.requireNonNull(data);
        this.random = Objects.requireNonNull(random);
        this.subFirers = List.copyOf(subFirers);
    }

    @Override
    public @NotNull Firer forPlayer(@NotNull ZombiesPlayer player, @NotNull InjectionStore injectionStore) {
        List<Firer> firerInstances = new ArrayList<>(subFirers.size());
        for (PlayerComponent<Firer> subFirer : subFirers) {
            firerInstances.add(subFirer.forPlayer(player, injectionStore));
        }

        return (start, previousHits) -> {
            if (subFirers.isEmpty()) {
                return;
            }

            if (data.angleVariance() == 0) {
                for (int i = 0; i < Math.max(subFirers.size(), data.amount()); i++) {
                    firerInstances.get(i % subFirers.size()).fire(start, previousHits);
                }

                return;
            }

            double yaw = start.yaw();
            double pitch = start.pitch();

            for (int i = 0; i < Math.max(subFirers.size(), data.amount()); i++) {
                double newYaw = yaw + data.angleVariance() * (2 * random.nextDouble() - 1);
                double newPitch = pitch + data.angleVariance() * (2 * random.nextDouble() - 1);
                firerInstances.get(i % firerInstances.size())
                    .fire(start.withView((float) newYaw, (float) newPitch), previousHits);
            }
        };
    }


    public record Data(
        @NotNull Collection<String> subFirers,
        int amount,
        float angleVariance) {
        @Default("amount")
        public static @NotNull ConfigElement defaultAmount() {
            return ConfigPrimitive.of(-1);
        }
    }

}
