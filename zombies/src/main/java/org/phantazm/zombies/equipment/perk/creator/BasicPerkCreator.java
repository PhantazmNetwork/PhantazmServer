package org.phantazm.zombies.equipment.perk.creator;

import com.github.steanky.element.core.annotation.DataObject;
import net.kyori.adventure.key.Key;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.perk.PerkLevel;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;

public class BasicPerkCreator implements PerkLevelCreator {
    private final Data data;

    public BasicPerkCreator(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @NotNull PerkLevel forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {

        return null;
    }

    private static class Level implements PerkLevel {
        private final Data data;
        private final ZombiesPlayer zombiesPlayer;

        private Level(Data data, ZombiesPlayer zombiesPlayer) {
            this.data = data;
            this.zombiesPlayer = zombiesPlayer;
        }

        @Override
        public @NotNull Key key() {
            return data.key;
        }

        @Override
        public void start() {

        }

        @Override
        public void tick(long time) {

        }

        @Override
        public void end() {

        }

        @Override
        public void setSelected(boolean selected) {

        }

        @Override
        public void rightClick() {

        }

        @Override
        public void leftClick() {

        }

        @Override
        public @NotNull ItemStack getItemStack() {
            return null;
        }

        @Override
        public boolean shouldRedraw() {
            return false;
        }
    }

    @DataObject
    public record Data(@NotNull Key key) {
    }
}
