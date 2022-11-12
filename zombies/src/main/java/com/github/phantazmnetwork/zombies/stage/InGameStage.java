package com.github.phantazmnetwork.zombies.stage;

import com.github.phantazmnetwork.commons.Activable;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.Wrapper;
import com.github.phantazmnetwork.core.inventory.InventoryAccess;
import com.github.phantazmnetwork.core.inventory.InventoryObjectGroup;
import com.github.phantazmnetwork.zombies.map.RoundHandler;
import com.github.phantazmnetwork.zombies.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class InGameStage extends StageBase {

    private final ZombiesMap map;

    private final Collection<? extends ZombiesPlayer> zombiesPlayers;

    private final Wrapper<Long> ticksSinceStart;

    public InGameStage(@NotNull Collection<Activable> activables,
            @NotNull Collection<? extends ZombiesPlayer> zombiesPlayers, @NotNull ZombiesMap map,
            @NotNull Wrapper<Long> ticksSinceStart) {
        super(activables);
        this.map = Objects.requireNonNull(map, "map");
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
        this.ticksSinceStart = Objects.requireNonNull(ticksSinceStart, "ticksSinceStart");
    }

    @Override
    public void tick(long time) {
        super.tick(time);
        map.tick(time);
        ticksSinceStart.apply(ticks -> ticks + 1);
    }

    @Override
    public void start() {
        super.start();
        RoundHandler roundHandler = map.roundHandler();
        if (roundHandler.roundCount() != 0) {
            roundHandler.setCurrentRound(0);
        }
        ticksSinceStart.set(0L);

        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            if (!zombiesPlayer.getModule().getInventoryAccessRegistry().hasCurrentAccess()) {
                continue;
            }

            InventoryAccess access = zombiesPlayer.getModule().getInventoryAccessRegistry().getCurrentAccess();
            InventoryObjectGroup objectGroup =
                    access.groups().get(Key.key(Namespaces.PHANTAZM, "inventory.access.gun")); // todo
            if (objectGroup == null) {
                continue;
            }

            for (Key equipmentKey : map.getData().settings().defaultEquipment()) {
                zombiesPlayer.getModule().getEquipmentCreator().createEquipment(equipmentKey)
                        .ifPresent(objectGroup::pushInventoryObject);
            }
        }
    }

    @Override
    public boolean shouldEnd() {
        return map.roundHandler().hasEnded();
    }

    @Override
    public boolean hasPermanentPlayers() {
        return true;
    }

}
