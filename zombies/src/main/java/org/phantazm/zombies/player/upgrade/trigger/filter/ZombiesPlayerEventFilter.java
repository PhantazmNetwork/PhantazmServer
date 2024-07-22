package org.phantazm.zombies.player.upgrade.trigger.filter;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.event.Event;
import net.minestom.server.event.trait.EntityEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.event.trait.ZombiesPlayerEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

@Model("zombies.upgrade.filter.zombies_player")
@Cache
public class ZombiesPlayerEventFilter implements EventFilterComponent {
    private final Data data;

    @FactoryMethod
    public ZombiesPlayerEventFilter(@NotNull Data data) {
        this.data = data;
    }


    @Override
    public @NotNull EventFilter apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(zombiesPlayer, data);
    }

    private static final class Internal implements EventFilter {
        private final ZombiesPlayer zombiesPlayer;
        private final Data data;

        private Internal(@NotNull ZombiesPlayer zombiesPlayer, @NotNull Data data) {
            this.zombiesPlayer = zombiesPlayer;
            this.data = data;
        }

        @Override
        public boolean test(Event event) {
            return switch (data.filterMode) {
                case ZOMBIES_PLAYER_MATCHES -> event instanceof ZombiesPlayerEvent zombiesPlayerEvent &&
                    zombiesPlayerEvent.zombiesPlayer() == zombiesPlayer;

                case UUID_MATCHES -> event instanceof EntityEvent entityEvent &&
                    entityEvent.getEntity().getUuid().equals(zombiesPlayer.getUUID());

                case EITHER_MATCHES -> (event instanceof ZombiesPlayerEvent zombiesPlayerEvent &&
                    zombiesPlayerEvent.zombiesPlayer() == zombiesPlayer) ||
                    (event instanceof EntityEvent entityEvent &&
                        entityEvent.getEntity().getUuid().equals(zombiesPlayer.getUUID()));
            };
        }
    }

    public enum FilterMode {
        ZOMBIES_PLAYER_MATCHES,
        UUID_MATCHES,
        EITHER_MATCHES
    }

    @Default("""
        {
          filterMode='EITHER_MATCHES'
        }
        """)
    @DataObject
    public record Data(@NotNull FilterMode filterMode) {

    }
}
