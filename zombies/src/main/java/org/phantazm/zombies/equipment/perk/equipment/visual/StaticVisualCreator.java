package org.phantazm.zombies.equipment.perk.equipment.visual;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.annotation.document.Description;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;

@Description("A perk visual that is a static, unchanging item.")
@Model("zombies.perk.visual.static")
@Cache
public class StaticVisualCreator implements PerkVisualCreator {
    private final Data data;

    @FactoryMethod
    public StaticVisualCreator(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull PerkVisual forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        return new Visual(data);
    }

    private static class Visual implements PerkVisual {
        private final Data data;

        private Visual(Data data) {
            this.data = Objects.requireNonNull(data);
        }

        @Override
        public @NotNull ItemStack computeItemStack() {
            return data.stack;
        }

        @Override
        public boolean shouldCompute() {
            return true;
        }

        @Override
        public void leftClick(boolean success) {

        }

        @Override
        public void rightClick(boolean success) {

        }

        @Override
        public void tick(long time) {

        }
    }

    @DataObject
    public record Data(@NotNull @Description("The item this visual will show") ItemStack stack) {
    }
}
