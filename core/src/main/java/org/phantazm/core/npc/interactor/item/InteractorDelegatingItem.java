package org.phantazm.core.npc.interactor.item;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.BasicComponent;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.gui.Gui;
import org.phantazm.core.gui.GuiItem;
import org.phantazm.core.npc.interactor.Interactor;

@Model("npc.interactor.gui.item")
@Cache
public class InteractorDelegatingItem implements BasicComponent<GuiItem> {
    private final Data data;
    private final BasicComponent<Interactor> interactor;

    @FactoryMethod
    public InteractorDelegatingItem(@NotNull Data data,
        @NotNull @Child("interactor") BasicComponent<Interactor> interactor) {
        this.data = data;
        this.interactor = interactor;
    }

    @Override
    public GuiItem apply(@NotNull InjectionStore injectionStore) {
        return new Internal(data, interactor.apply(injectionStore));
    }

    private record Internal(Data data,
        Interactor interactor) implements GuiItem {
        @Override
        public void handleClick(@NotNull Gui owner, @NotNull Player player, int slot, @NotNull ClickType clickType) {
            if (clickType == ClickType.LEFT_CLICK) {
                interactor.interact(player);

                if (data.closeOnClick) {
                    player.closeInventory();
                }
            }
        }

        @Override
        public void onRemove(@NotNull Gui owner, int slot) {

        }

        @Override
        public void onReplace(@NotNull Gui owner, @NotNull GuiItem newItem, int slot) {

        }

        @Override
        public @NotNull ItemStack getItemStack() {
            return data.itemStack;
        }

        @Override
        public boolean shouldRedraw() {
            return false;
        }
    }

    @DataObject
    public record Data(
        @NotNull ItemStack itemStack,
        boolean closeOnClick,
        @NotNull @ChildPath("interactor") String interactor) {
        @Default("closeOnClick")
        public static @NotNull ConfigElement defaultCloseOnClick() {
            return ConfigPrimitive.of(true);
        }
    }
}
