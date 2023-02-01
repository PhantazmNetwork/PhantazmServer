package org.phantazm.zombies.perk;

import com.github.steanky.element.core.annotation.ChildPath;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.item.UpdatingItem;
import org.phantazm.zombies.equipment.Equipment;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.shoot.fire.Firer;
import org.phantazm.zombies.equipment.gun.shoot.handler.ShotHandler;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class AddShotHandlerLevel extends PerkLevelBase {

    private final Map<Firer, ShotHandler> registeredHandlers = new IdentityHashMap<>();

    private final ShotHandler shotHandler;

    private final ZombiesPlayer user;

    public AddShotHandlerLevel(@NotNull Data data, @NotNull UpdatingItem item, @NotNull ShotHandler shotHandler,
            @NotNull ZombiesPlayer user) {
        super(data, item);
        this.shotHandler = Objects.requireNonNull(shotHandler, "shotHandler");
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
        for (Equipment equipment : user.module().getEquipment()) {
            if (!(equipment instanceof Gun gun) || registeredHandlers.containsKey(gun.getLevel().firer())) {
                continue;
            }

            registeredHandlers.put(gun.getLevel().firer(), shotHandler);
        }
    }

    @Override
    public void end() {
        for (Map.Entry<Firer, ShotHandler> registeredHandler : registeredHandlers.entrySet()) {
            registeredHandler.getKey().removeExtraShotHandler(registeredHandler.getValue());
        }
    }

    @Model("zombies.perk.level.add_shot_handler")
    public record Creator(@NotNull Data data,
                          @NotNull @ChildPath("updating_item") UpdatingItem item,
                          @NotNull @ChildPath("shot_handler") ShotHandler shotHandler) implements PerkCreator {

        @FactoryMethod
        public Creator {

        }

        @Override
        public PerkLevel createPerk(@NotNull ZombiesPlayer user) {
            return new AddShotHandlerLevel(data, item, shotHandler, user);
        }
    }

    @DataObject
    public record Data(@NotNull Key levelKey,
                       @NotNull Set<Key> upgrades,
                       @NotNull @ChildPath("updating_item") String updatingItemPath,
                       @NotNull @ChildPath("shot_handler") String shotHandlerPath) implements PerkData {

    }
}
