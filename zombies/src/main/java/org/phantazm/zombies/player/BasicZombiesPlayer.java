package org.phantazm.zombies.player;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.inventory.InventoryAccessRegistry;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.core.inventory.InventoryProfile;
import org.phantazm.zombies.map.Flaggable;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("ClassCanBeRecord")
public class BasicZombiesPlayer implements ZombiesPlayer, ForwardingAudience {

    private final ZombiesScene scene;

    private final ZombiesPlayerModule module;

    private double reviveSpeedMultiplier = 1.0F;

    public BasicZombiesPlayer(@NotNull ZombiesScene scene, @NotNull ZombiesPlayerModule module) {
        this.scene = Objects.requireNonNull(scene, "scene");
        this.module = Objects.requireNonNull(module, "module");
    }

    @Override
    public @NotNull ZombiesPlayerModule module() {
        return module;
    }

    @Override
    public long getReviveTime() {
        return (long)(30L * reviveSpeedMultiplier);
    }

    @Override
    public double getReviveSpeedMultiplier() {
        return reviveSpeedMultiplier;
    }

    @Override
    public void setReviveSpeedMultiplier(double multiplier) {
        this.reviveSpeedMultiplier = multiplier;
    }

    @Override
    public @NotNull ZombiesScene getScene() {
        return scene;
    }

    @Override
    public void start() {
        module.getStateSwitcher().start();
    }

    @Override
    public void tick(long time) {
        Optional<Player> playerOptional = getPlayer();
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            module.getMeta().setCrouching(player.getPose() == Entity.Pose.SNEAKING);

            inventoryTick(player, time);
        }
        else {
            module.getMeta().setCrouching(false);
        }

        module.getStateSwitcher().tick(time);
    }

    @Override
    public void end() {
        if (!isState(ZombiesPlayerStateKeys.QUIT)) {
            getPlayer().ifPresent(player -> player.getInventory().clear());
        }
        module.getStateSwitcher().end();
    }

    private void inventoryTick(Player player, long time) {
        InventoryAccessRegistry accessRegistry = module.getInventoryAccessRegistry();
        if (accessRegistry.hasCurrentAccess()) {
            InventoryProfile profile = accessRegistry.getCurrentAccess().profile();
            for (int slot = 0; slot < profile.getSlotCount(); ++slot) {
                if (profile.hasInventoryObject(slot)) {
                    InventoryObject inventoryObject = profile.getInventoryObject(slot);
                    inventoryObject.tick(time);

                    if (inventoryObject.shouldRedraw()) {
                        player.getInventory().setItemStack(slot, inventoryObject.getItemStack());
                    }
                }
            }
        }
    }

    @Override
    public @NotNull Flaggable flags() {
        return module.flaggable();
    }


    @Override
    public @NotNull Iterable<? extends Audience> audiences() {
        Optional<Player> playerOptional = getPlayer();
        if (playerOptional.isEmpty()) {
            return List.of();
        }

        return List.of(playerOptional.get());
    }
}
