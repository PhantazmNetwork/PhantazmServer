package org.phantazm.zombies.listener;

import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Map;
import java.util.function.Supplier;

public class PlayerBlockInteractListener extends ZombiesPlayerEventListener<PlayerBlockInteractEvent> {

    public PlayerBlockInteractListener(@NotNull Instance instance,
        @NotNull Map<PlayerView, ZombiesPlayer> zombiesPlayers, @NotNull Supplier<ZombiesScene> scene) {
        super(instance, zombiesPlayers, scene);
    }

    @Override
    public void accept(@NotNull ZombiesScene scene, @NotNull ZombiesPlayer zombiesPlayer,
        @NotNull PlayerBlockInteractEvent event) {
        if (blockNextHandAnimation(event.getBlock())) {
            zombiesPlayer.setBlockHandAnimation();
        }
    }

    private static boolean blockNextHandAnimation(Block block) {
        String namespace = block.registry().namespace().toString();
        if (namespace.startsWith("potted_")) {
            return true;
        }

        Material material = block.registry().material();
        if (material == null) {
            return false;
        }

        String name = material.name();
        if (name.endsWith("_button") || name.endsWith("_fence_gate") ||
            (name.endsWith("_door") && !material.equals(Material.IRON_DOOR)) ||
            (name.endsWith("_trapdoor") && !material.equals(Material.IRON_TRAPDOOR)) || name.endsWith("_bed") ||
            name.endsWith("_table") || name.endsWith("chest") || name.endsWith("anvil") || name.endsWith("furnace")
            || name.endsWith("shulker_box")) {
            return true;
        }

        return material.equals(Material.LEVER) || material.equals(Material.BEACON) ||
            material.equals(Material.LOOM) || material.equals(Material.NOTE_BLOCK) ||
            material.equals(Material.BARREL) || material.equals(Material.HOPPER) ||
            material.equals(Material.COMPOSTER) || material.equals(Material.DISPENSER);
    }
}
