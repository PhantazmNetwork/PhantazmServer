package org.phantazm.zombies.perk;

import com.github.steanky.element.core.annotation.ChildPath;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.item.UpdatingItem;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class SpeedLevel extends PerkLevelBase {

    public static final UUID SPEED_UUID = UUID.fromString("2a82a2e2-5bec-4d61-b063-1ab89a1a1f1b");

    private final ZombiesPlayer user;

    public SpeedLevel(@NotNull PerkData data, @NotNull UpdatingItem item, @NotNull ZombiesPlayer user) {
        super(data, item);
        this.user = Objects.requireNonNull(user, "user");
    }

    @Override
    protected @NotNull Data getData() {
        return (Data)super.getData();
    }

    @Override
    public void start() {

    }

    @Override
    public void tick(long time) {
        super.tick(time);
        user.getPlayer().ifPresent(player -> {
            player.getAttribute(Attribute.MOVEMENT_SPEED).addModifier(
                    new AttributeModifier(SPEED_UUID, "speed-perk", getData().amount(),
                            AttributeOperation.MULTIPLY_TOTAL));
        });
    }

    @Override
    public void end() {
        user.getPlayer().ifPresent(player -> {
            player.getAttribute(Attribute.MOVEMENT_SPEED).removeModifier(SPEED_UUID);
        });
    }

    @Model("zombies.perk.level.speed")
    public record Creator(@NotNull Data data, @NotNull UpdatingItem item) implements PerkCreator {

        @FactoryMethod
        public Creator {

        }

        @Override
        public PerkLevel createPerk(@NotNull ZombiesPlayer user) {
            return new SpeedLevel(data, item, user);
        }
    }

    @DataObject
    public record Data(@NotNull Key levelKey,
                       @NotNull Set<Key> upgrades,
                       double amount,
                       @NotNull @ChildPath("updating_item") String updatingItemPath) implements PerkData {

    }

}
