package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.tick.Activable;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;
import java.util.UUID;

@Model("zombies.upgrade.player_attribute")
@Cache
public class PlayerAttributeUpgrade implements PlayerUpgradeComponent {
    private final Data data;

    @FactoryMethod
    public PlayerAttributeUpgrade(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull PlayerUpgrade apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer player) {
        return new Internal(player, data);
    }

    private static class Internal extends GuardedPlayerUpgrade {
        private final ZombiesPlayer zombiesPlayer;

        private final Activable state;

        private Internal(ZombiesPlayer zombiesPlayer, Data data) {
            this.zombiesPlayer = zombiesPlayer;

            this.state = Activable.threadsafeWrapper(new Activable() {
                private final UUID uuid = UUID.randomUUID();
                private final String uuidString = uuid.toString();

                @Override
                public void start() {
                    zombiesPlayer.getPlayer().ifPresent(player -> {
                        player.getAttribute(Objects.requireNonNullElse(Attribute.fromKey(data.attribute), Attributes.NIL))
                            .addModifier(new AttributeModifier(uuid, uuidString, data.amount, data.attributeOperation));
                    });
                }

                @Override
                public void end() {
                    zombiesPlayer.getPlayer().ifPresent(player -> {
                        player.getAttribute(Objects.requireNonNullElse(Attribute.fromKey(data.attribute), Attributes.NIL))
                            .removeModifier(uuid);
                    });
                }
            });
        }

        @Override
        public boolean needsTicking() {
            return false;
        }

        @Override
        public void startGuarded() {
            zombiesPlayer.addActivable(state);
        }

        @Override
        public void endGuarded() {
            zombiesPlayer.removeActivable(state);
        }
    }

    @DataObject
    public record Data(
        @NotNull String attribute,
        double amount,
        @NotNull AttributeOperation attributeOperation) {

    }
}
