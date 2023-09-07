package org.phantazm.zombies.equipment.gun2.visual;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.equipment.gun2.GunModule;
import org.phantazm.zombies.equipment.gun2.GunState;
import org.phantazm.zombies.equipment.gun2.Keys;
import org.phantazm.zombies.equipment.gun2.reload.ReloadTester;
import org.phantazm.zombies.player.PlayerComponent;
import org.phantazm.zombies.player.ZombiesPlayer;

public class ClipStackMapper implements PlayerComponent<GunStackMapper> {
    @Override
    public @NotNull GunStackMapper forPlayer(@NotNull ZombiesPlayer player, @NotNull InjectionStore injectionStore) {
        GunModule module = injectionStore.get(Keys.GUN_MODULE);
        return new Mapper(module.reloadTester(), module.state());
    }

    private static class Mapper implements GunStackMapper {

        private final ReloadTester reloadTester;

        private final GunState state;

        public Mapper(ReloadTester reloadTester, GunState state) {
            this.reloadTester = reloadTester;
            this.state = state;
        }

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack stack) {
            if (!reloadTester.isReloading()) {
                return stack.withAmount(Math.max(1, state.getClip()));
            }

            return stack;
        }
    }

}
