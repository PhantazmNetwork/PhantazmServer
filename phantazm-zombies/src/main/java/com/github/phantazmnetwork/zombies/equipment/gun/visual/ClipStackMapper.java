package com.github.phantazmnetwork.zombies.equipment.gun.visual;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.ReloadTester;
import net.kyori.adventure.key.Key;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ClipStackMapper implements GunStackMapper {

    public record Data(@NotNull Key reloadTesterKey) implements VariantSerializable {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.stack_mapper.clip.stack_count");

        @Override
        public @NotNull Key getSerialKey() {
            return SERIAL_KEY;
        }
    }

    private final Data data;

    private final ReloadTester reloadTester;

    public ClipStackMapper(@NotNull Data data, @NotNull ReloadTester reloadTester) {
        this.reloadTester = Objects.requireNonNull(reloadTester, "reloadTester");
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @NotNull ItemStack map(@NotNull GunState state, @NotNull ItemStack intermediate) {
        if (!reloadTester.isReloading(state)) {
            return intermediate.withAmount(Math.max(1, state.clip()));
        }

        return intermediate;
    }

    @Override
    public @NotNull VariantSerializable getData() {
        return data;
    }

}
