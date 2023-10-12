package org.phantazm.zombies.npc;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.key.Constants;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.commons.MonoComponent;
import org.phantazm.core.gui.BasicSlotDistributor;
import org.phantazm.core.gui.SlotDistributor;
import org.phantazm.core.npc.interactor.NPCInteractor;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.zombies.modifier.ModifierComponent;
import org.phantazm.zombies.modifier.ModifierHandler;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Model("zombies.npc.interactor.modifier_gui")
@Cache
public class ModifierGuiInteractor implements MonoComponent<NPCInteractor> {
    private final Data data;

    @FactoryMethod
    public ModifierGuiInteractor(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull NPCInteractor apply(@NotNull InjectionStore injectionStore) {
        return new Impl(data);
    }

    @DataObject
    public record Data(int modifierTogglePadding,
        @NotNull String titleFormat,
        @NotNull ItemStack inactiveItem,
        @NotNull ItemStack activeItem,
        @NotNull ItemStack aboutItem,
        @NotNull ItemStack previousItem,
        @NotNull ItemStack nextItem,
        @NotNull ItemStack clearItem,
        @NotNull Component enabledMessage,
        @NotNull Component disabledMessage,
        @NotNull Component disabledAllMessage,
        @NotNull String conflictingModifiersFormat,
        @NotNull Component invalidModifierMessage,
        @NotNull Sound successSound,
        @NotNull Sound failureSound) {
        @Default("modifierTogglePadding")
        public static @NotNull ConfigElement defaultModifierTogglePadding() {
            return ConfigPrimitive.of(0);
        }
    }

    private static class Impl implements NPCInteractor {
        private static final Tag<String> MODIFIER_TAG = Tag.String("modifier");
        private static final Tag<String> ACTION_TAG = Tag.String("action");

        private static final String PREVIOUS_ACTION = "previous";
        private static final String NEXT_ACTION = "next";
        private static final String CLEAR_ACTION = "clear";

        private final Data data;
        private final SlotDistributor slotDistributor;

        private final List<ModifierComponent> modifiers;
        private final int pages;

        private static final int MODIFIERS_PER_PAGE = 9;
        private static final int CHEST_WIDTH = 9;

        private Impl(Data data) {
            this.data = data;
            this.slotDistributor = new BasicSlotDistributor(data.modifierTogglePadding);

            ModifierHandler handler = ModifierHandler.Global.instance();
            Map<Key, ModifierComponent> components = handler.componentMap();

            this.modifiers = components.values().stream().sorted(Comparator.comparing(ModifierComponent::ordinal))
                .toList();
            this.pages = (int) Math.ceil(this.modifiers.size() / (double) MODIFIERS_PER_PAGE);
        }

        @Override
        public void interact(@NotNull Player player) {
            showPage(player, 0);
        }

        private void showPage(Player player, int page) {
            if (modifiers.isEmpty()) {
                return; //no modifiers to show
            }

            //slots to put modifier toggle pairs in
            int[] slots = slotDistributor.distribute(MODIFIERS_PER_PAGE, 1,
                Math.min(modifiers.size() - (page * MODIFIERS_PER_PAGE), MODIFIERS_PER_PAGE));

            PlayerView playerView = PlayerViewProvider.Global.instance().fromPlayer(player);
            ModifierHandler modifierHandler = ModifierHandler.Global.instance();

            LocalInventory inventory = new LocalInventory(InventoryType.CHEST_6_ROW, computeTitle(playerView), playerView, page);
            inventory.addInventoryCondition((player1, slot, clickType, inventoryConditionResult) -> {
                if (clickType != ClickType.LEFT_CLICK) {
                    inventoryConditionResult.setCancel(true);
                }
            });

            inventory.setItemStack(45, data.aboutItem);
            inventory.setItemStack(53, data.clearItem.withTag(ACTION_TAG, CLEAR_ACTION));

            if (page != 0) inventory.setItemStack(48, data.previousItem.withTag(ACTION_TAG, PREVIOUS_ACTION));
            if (page < pages - 1) inventory.setItemStack(50, data.nextItem.withTag(ACTION_TAG, NEXT_ACTION));


            Set<Key> enabledModifiers = modifierHandler.getModifiers(playerView);
            for (int i = 0; i < slots.length; i++) {
                int slot = slots[i];
                ModifierComponent component = modifiers.get((page * MODIFIERS_PER_PAGE) + i);
                int modifierSlot = slot + CHEST_WIDTH;
                int displaySlot = slot + (CHEST_WIDTH * 2);

                String modifierString = component.key().asString();
                inventory.setItemStack(modifierSlot, component.displayItem().withTag(MODIFIER_TAG,
                    modifierString));

                inventory.setItemStack(displaySlot, (enabledModifiers.contains(component.key()) ? data.activeItem :
                    data.inactiveItem).withTag(MODIFIER_TAG, modifierString));
            }

            player.openInventory(inventory);
        }

        private Component computeTitle(PlayerView playerView) {
            ModifierHandler modifierHandler = ModifierHandler.Global.instance();
            int modifierCount = modifierHandler.enabledModifierCount(playerView);
            return MiniMessage.miniMessage().deserialize(data.titleFormat,
                Placeholder.unparsed("modifier_count", Integer.toString(modifierCount)));
        }

        private class LocalInventory extends Inventory {
            private final PlayerView view;
            private final int page;

            private LocalInventory(@NotNull InventoryType inventoryType, @NotNull Component title, @NotNull PlayerView view, int page) {
                super(inventoryType, title);
                this.view = view;
                this.page = page;
            }

            @Override
            public boolean leftClick(@NotNull Player player, int slot) {
                if (slot > getInventoryType().getSize()) {
                    return false;
                }

                ItemStack stack = getItemStack(slot);

                ModifierHandler modifierHandler = ModifierHandler.Global.instance();
                @Subst(Constants.NAMESPACE_OR_KEY) String modifier = stack.getTag(MODIFIER_TAG);
                if (modifier != null) {
                    Key key = Key.key(modifier);
                    ModifierHandler.ModifierResult result = modifierHandler.toggleModifier(view, key);
                    handleResult(player, result, key, slot);

                    return false;
                }

                String action = stack.getTag(ACTION_TAG);
                if (action == null) {
                    return false;
                }

                int nextPage;
                switch (action) {
                    case PREVIOUS_ACTION, NEXT_ACTION -> {
                        nextPage = action.equals(PREVIOUS_ACTION) ? page - 1 : page + 1;
                        if (nextPage < 0 || nextPage >= pages) {
                            player.playSound(data.failureSound);
                            return false;
                        }
                    }
                    case CLEAR_ACTION -> {
                        nextPage = page;
                        if (!modifierHandler.hasAnyModifiers(view)) {
                            player.playSound(data.failureSound);
                            return false;
                        }

                        player.playSound(data.successSound);
                        player.sendMessage(data.disabledAllMessage);
                        modifierHandler.clearModifiers(view);
                    }
                    default -> {
                        return false;
                    }
                }

                player.scheduleNextTick(self -> {
                    Player selfPlayer = (Player) self;
                    showPage(selfPlayer, nextPage);
                });

                return false;
            }

            private void handleResult(Player player, ModifierHandler.ModifierResult result,
                Key modifierKey, int slot) {
                switch (result.status()) {
                    case MODIFIER_ENABLED -> {
                        player.playSound(data.successSound);
                        player.sendMessage(data.enabledMessage);
                        setItemStack(slot > 17 ? slot : slot + CHEST_WIDTH, data.activeItem.withTag(MODIFIER_TAG, modifierKey.asString()));
                        setTitle(computeTitle(view));
                    }
                    case MODIFIER_DISABLED -> {
                        player.playSound(data.successSound);
                        player.sendMessage(data.disabledMessage);
                        setItemStack(slot > 17 ? slot : slot + CHEST_WIDTH, data.inactiveItem.withTag(MODIFIER_TAG, modifierKey.asString()));
                        setTitle(computeTitle(view));
                    }
                    case CONFLICTING_MODIFIERS -> {
                        player.playSound(data.failureSound);

                        TagResolver resolver = Placeholder.component("conflicts", Component.join(
                            JoinConfiguration.commas(true), result.conflictingModifiers().stream()
                                .map(ModifierComponent::displayName).toList()));

                        player.sendMessage(MiniMessage.miniMessage().deserialize(data.conflictingModifiersFormat, resolver));
                    }
                    case INVALID_MODIFIER -> {
                        player.playSound(data.failureSound);
                        player.sendMessage(data.invalidModifierMessage);
                    }
                }
            }
        }
    }
}